import qrcode
from io import BytesIO
import base64
import uuid
import os
from datetime import datetime, timedelta
from typing import Optional, Tuple
from PIL import Image
from sqlalchemy.orm import Session
from app.models import User, QRCode, UserVehicle
from app.config import settings
import json
from cryptography.fernet import Fernet, InvalidToken


def _get_fernet():
	key = getattr(settings, 'qr_encryption_key', None)
	if key:
		try:
			return Fernet(key)
		except Exception:
			return None
	return None

def encrypt_qr_payload(data: dict) -> str:
	cipher = _get_fernet()
	payload = json.dumps(data).encode()
	if cipher:
		return cipher.encrypt(payload).decode()
	return base64.b64encode(payload).decode()

def decrypt_qr_payload(token: str) -> Optional[dict]:
	cipher = _get_fernet()
	try:
		if cipher:
			decoded = cipher.decrypt(token.encode())
		else:
			decoded = base64.b64decode(token.encode())
		return json.loads(decoded.decode())
	except (InvalidToken, Exception):
		return None

def generate_qr_code_data(user_id: int, phone: str, vehicle: Optional[str], qr_type: str = "mobile") -> str:
	"""Generate unique QR code data for a user."""
	unique_id = str(uuid.uuid4())
	# Use short keys to save space (String(255) limit)
	qr_data = {
		"u": user_id,
        "p": phone,
        "v": vehicle,
		"q": unique_id,
		"t": qr_type,
		"c": datetime.utcnow().isoformat() + "Z"
	}
	return encrypt_qr_payload(qr_data)

def create_qr_code_image(qr_data: str, size: int = 300) -> Tuple[str, str]:
    """Create QR code image and return base64 encoded image and file path."""
    qr = qrcode.QRCode(
        version=1,
        error_correction=qrcode.constants.ERROR_CORRECT_L,
        box_size=10,
        border=4,
    )
    qr.add_data(qr_data)
    qr.make(fit=True)
    
    # Create QR code image
    qr_image = qr.make_image(fill_color="black", back_color="white")
    
    # Resize image
    qr_image = qr_image.resize((size, size), Image.Resampling.LANCZOS)
    
    # Save to file
    filename = f"qr_{uuid.uuid4().hex}.png"
    file_path = os.path.join(settings.upload_dir, "qr_codes", filename)
    os.makedirs(os.path.dirname(file_path), exist_ok=True)
    qr_image.save(file_path)
    
    # Convert to base64 for API response
    buffer = BytesIO()
    qr_image.save(buffer, format='PNG')
    base64_image = base64.b64encode(buffer.getvalue()).decode()
    
    return base64_image, file_path

def generate_user_qr_code(db: Session, user_id: int, qr_type: str = "mobile", 
                         validity_hours: Optional[int] = None,
                         vehicle_id: Optional[int] = None) -> QRCode:
    """Generate and store QR code for a user."""
    # Get user details
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise ValueError("User not found")

    # If vehicle_id is provided, get vehicle details
    vehicle_number = None
    vehicle = None
    if vehicle_id:
        vehicle = db.query(UserVehicle).filter(
            UserVehicle.id == vehicle_id,
            UserVehicle.user_id == user_id
        ).first()
        if not vehicle:
            raise ValueError("Vehicle not found")
        vehicle_number = vehicle.vehicle_number

    # Deactivate existing QR codes for this vehicle+type combo
    filter_conditions = [
        QRCode.user_id == user_id,
        QRCode.qr_type == qr_type,
        QRCode.is_active == True
    ]
    if vehicle_id:
        filter_conditions.append(QRCode.vehicle_id == vehicle_id)
    
    existing_qrs = db.query(QRCode).filter(*filter_conditions).all()
    for qr in existing_qrs:
        qr.is_active = False

    # Generate new QR code data
    qr_data = generate_qr_code_data(user_id, user.phone_number, vehicle_number, qr_type)
    
    # Create QR code image
    base64_image, file_path = create_qr_code_image(qr_data)
    
    # Calculate expiry date if validity hours provided
    expires_at = None
    if validity_hours:
        expires_at = datetime.utcnow() + timedelta(hours=validity_hours)
    
    # Store in database
    qr_code = QRCode(
        user_id=user_id,
        vehicle_id=vehicle_id,
        qr_code=qr_data,
        qr_image_path=file_path,
        qr_type=qr_type,
        expires_at=expires_at
    )
    
    db.add(qr_code)
    db.commit()
    db.refresh(qr_code)
    
    return qr_code

def validate_qr_code(db: Session, qr_data: str) -> Optional[Tuple[User, QRCode]]:
    """Validate QR code and return user and QR code objects."""
    try:
        qr_data = qr_data.strip()
        print(f"Validating QR: {qr_data[:20]}... (Len: {len(qr_data)})")
        # Find QR code in database
        qr_record = db.query(QRCode).filter(
            QRCode.qr_code == qr_data,
            QRCode.is_active == True
        ).first()
        
        if not qr_record:
            print("QR Validation Failed: Record not found or inactive")
            # Debug: Check if it exists but is inactive
            inactive = db.query(QRCode).filter(QRCode.qr_code == qr_data).first()
            if inactive:
                print(f"  -> Found inactive record (ID: {inactive.id}, Active: {inactive.is_active})")
            else:
                print("  -> No record found with this exact string.")
            return None
        
        # Check if QR code is expired
        if qr_record.expires_at and qr_record.expires_at < datetime.utcnow():
            print(f"QR Validation Failed: Expired at {qr_record.expires_at}")
            return None
        
        # Get user
        user = db.query(User).filter(User.id == qr_record.user_id).first()
        if not user or not user.is_active:
            print("QR Validation Failed: User not found or inactive")
            return None
        
        print(f"QR Validated: User {user.id}")
        return user, qr_record
        
    except Exception as e:
        print(f"Error validating QR code: {e}")
        return None

def decode_qr_data(qr_data: str) -> Optional[dict]:
    """Decode QR code data to extract information."""
    return decrypt_qr_payload(qr_data)

def get_user_qr_codes(db: Session, user_id: int, active_only: bool = True) -> list:
    """Get all QR codes for a user."""
    query = db.query(QRCode).filter(QRCode.user_id == user_id)
    
    if active_only:
        query = query.filter(QRCode.is_active == True)
    
    # Also filter out expired QR codes if active_only is True
    if active_only:
        query = query.filter(
            (QRCode.expires_at.is_(None)) | 
            (QRCode.expires_at > datetime.utcnow())
        )
    
    return query.order_by(QRCode.created_at.desc()).all()

def deactivate_qr_code(db: Session, qr_code_id: int, user_id: int) -> bool:
    """Deactivate a specific QR code."""
    qr_code = db.query(QRCode).filter(
        QRCode.id == qr_code_id,
        QRCode.user_id == user_id
    ).first()
    
    if not qr_code:
        return False
    
    qr_code.is_active = False
    db.commit()
    return True

def get_qr_code_image(qr_code_id: int, user_id: int, db: Session) -> Optional[str]:
    """Get QR code image as base64 string."""
    qr_code = db.query(QRCode).filter(
        QRCode.id == qr_code_id,
        QRCode.user_id == user_id
    ).first()
    
    if not qr_code or not qr_code.qr_image_path:
        return None
    
    try:
        if os.path.exists(qr_code.qr_image_path):
            with open(qr_code.qr_image_path, "rb") as image_file:
                base64_image = base64.b64encode(image_file.read()).decode()
                return base64_image
    except Exception as e:
        print(f"Error reading QR code image: {e}")
    
    return None 