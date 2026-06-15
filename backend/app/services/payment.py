import asyncio
import json
import logging
import uuid
from datetime import datetime
from typing import Any, Dict, Optional

import razorpay
import stripe
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.config import settings
from app.models import (
    PetrolPump,
    QRCode,
    Transaction,
    TransactionStatus,
    TransactionType,
    User,
    Wallet,
)
from app.services.notifications import notify_fuel_purchase, notify_wallet_recharge

logger = logging.getLogger("zappay.services.payment")

# ── Payment Gateway Clients ────────────────────────────────────────────
razorpay_client = None
if settings.razorpay_key_id and settings.razorpay_key_secret:
    razorpay_client = razorpay.Client(auth=(settings.razorpay_key_id, settings.razorpay_key_secret))
    logger.info("Razorpay client initialized")

if settings.stripe_secret_key:
    stripe.api_key = settings.stripe_secret_key
    logger.info("Stripe client initialized")


def generate_transaction_id() -> str:
    return f"ZP{uuid.uuid4().hex[:12].upper()}"


def get_or_create_wallet(db: Session, user_id: int) -> Wallet:
    wallet = db.query(Wallet).filter(Wallet.user_id == user_id).first()
    if not wallet:
        wallet = Wallet(user_id=user_id)
        db.add(wallet)
        db.commit()
        db.refresh(wallet)
    return wallet


def check_wallet_balance(db: Session, user_id: int, amount: float) -> bool:
    wallet = get_or_create_wallet(db, user_id)
    return wallet.balance >= amount


def debit_wallet(db: Session, user_id: int, amount: float, transaction_type: TransactionType = TransactionType.FUEL_PURCHASE) -> Optional[Transaction]:
    wallet = get_or_create_wallet(db, user_id)
    if wallet.balance < amount:
        return None
    transaction = Transaction(
        transaction_id=generate_transaction_id(),
        user_id=user_id,
        wallet_id=wallet.id,
        transaction_type=transaction_type,
        amount=amount,
        status=TransactionStatus.PENDING,
    )
    db.add(transaction)
    wallet.balance -= amount
    wallet.total_spent += amount
    db.commit()
    db.refresh(transaction)
    return transaction


def credit_wallet(db: Session, user_id: int, amount: float, transaction_type: TransactionType = TransactionType.WALLET_RECHARGE) -> Transaction:
    wallet = get_or_create_wallet(db, user_id)
    transaction = Transaction(
        transaction_id=generate_transaction_id(),
        user_id=user_id,
        wallet_id=wallet.id,
        transaction_type=transaction_type,
        amount=amount,
        status=TransactionStatus.COMPLETED,
    )
    db.add(transaction)
    wallet.balance += amount
    if transaction_type == TransactionType.WALLET_RECHARGE:
        wallet.total_recharged += amount
    db.commit()
    db.refresh(transaction)
    return transaction


# ── Payment Gateway Operations ─────────────────────────────────────────
def create_razorpay_order(amount: float, currency: str = "INR", user_id: Optional[int] = None) -> Optional[Dict]:
    if not razorpay_client:
        logger.warning("Razorpay not configured")
        return None
    try:
        order = razorpay_client.order.create({
            "amount": int(amount * 100),
            "currency": currency,
            "payment_capture": 1,
            "notes": {"user_id": str(user_id) if user_id else ""},
        })
        logger.info("Razorpay order created: %s", order.get("id"))
        return order
    except Exception as e:
        logger.error("Razorpay order creation failed: %s", e)
        return None


def verify_razorpay_payment(payment_id: str, order_id: str, signature: str) -> bool:
    if not razorpay_client:
        return False
    try:
        razorpay_client.utility.verify_payment_signature({
            "razorpay_order_id": order_id,
            "razorpay_payment_id": payment_id,
            "razorpay_signature": signature,
        })
        return True
    except Exception as e:
        logger.error("Razorpay verification failed: %s", e)
        return False


def create_stripe_payment_intent(amount: float, currency: str = "inr", user_id: Optional[int] = None) -> Optional[Dict]:
    try:
        intent = stripe.PaymentIntent.create(
            amount=int(amount * 100),
            currency=currency,
            automatic_payment_methods={"enabled": True},
            metadata={"user_id": str(user_id) if user_id else ""},
        )
        logger.info("Stripe payment intent created: %s", intent.get("id"))
        return intent
    except Exception as e:
        logger.error("Stripe payment intent creation failed: %s", e)
        return None


# ── Fuel Purchase ──────────────────────────────────────────────────────
def process_fuel_purchase(db: Session, qr_code: str, pump_id: int, fuel_type: str, fuel_quantity: float, fuel_rate: float) -> Dict[str, Any]:
    from app.services.qr_service import validate_qr_code

    qr_validation = validate_qr_code(db, qr_code)
    if not qr_validation:
        logger.warning("Fuel purchase: QR validation failed")
        return {"success": False, "message": "Invalid QR code"}

    user, qr_record = qr_validation
    return _process_payment(db, user, pump_id, fuel_type, fuel_quantity, fuel_rate, qr_record.id)


def process_fuel_purchase_by_user(db: Session, user_id: int, pump_id: int, fuel_type: str, fuel_quantity: float, fuel_rate: float) -> Dict[str, Any]:
    user = db.query(User).filter(User.id == user_id, User.is_active == True).first()
    if not user:
        return {"success": False, "message": "User not found"}
    return _process_payment(db, user, pump_id, fuel_type, fuel_quantity, fuel_rate, qr_code_id=None)


def _process_payment(db: Session, user: User, pump_id: int, fuel_type: str, fuel_quantity: float, fuel_rate: float, qr_code_id: Optional[int] = None) -> Dict[str, Any]:
    pump = db.query(PetrolPump).filter(PetrolPump.id == pump_id, PetrolPump.is_active == True).first()
    if not pump:
        logger.warning("Pump not found: id=%d", pump_id)
        return {"success": False, "message": "Invalid petrol pump"}

    total_amount = round(fuel_quantity * fuel_rate, 2)
    commission_amount = round(total_amount * pump.commission_rate, 2)

    wallet = get_or_create_wallet(db, user.id)
    if wallet.balance < total_amount:
        return {"success": False, "message": "Insufficient wallet balance"}

    transaction = Transaction(
        transaction_id=generate_transaction_id(),
        user_id=user.id,
        wallet_id=wallet.id,
        qr_code_id=qr_code_id,
        pump_id=pump_id,
        transaction_type=TransactionType.FUEL_PURCHASE,
        amount=total_amount,
        commission_amount=commission_amount,
        fuel_quantity=fuel_quantity,
        fuel_type=fuel_type,
        fuel_rate=fuel_rate,
        status=TransactionStatus.PENDING,
    )
    db.add(transaction)

    try:
        wallet.balance -= total_amount
        wallet.total_spent += total_amount
        transaction.status = TransactionStatus.COMPLETED
        transaction.completed_at = datetime.utcnow()
        db.commit()
        db.refresh(transaction)

        asyncio.create_task(notify_fuel_purchase(
            user.id, user.phone_number, user.email,
            total_amount, wallet.balance, transaction.transaction_id,
            pump.pump_name, fuel_type, fuel_quantity,
        ))

        logger.info("Fuel purchase completed: txn=%s user=%s pump=%s amount=%.2f", transaction.transaction_id, user.id, pump_id, total_amount)
        return {
            "success": True,
            "message": "Fuel purchase successful",
            "transaction_id": transaction.transaction_id,
            "amount": total_amount,
            "fuel_quantity": fuel_quantity,
            "fuel_type": fuel_type,
            "remaining_balance": wallet.balance,
        }
    except Exception as e:
        db.rollback()
        logger.error("Fuel purchase failed: %s", e)
        return {"success": False, "message": f"Transaction failed: {e}"}


# ── Transaction History ────────────────────────────────────────────────
def get_transaction_history(db: Session, user_id: int, page: int = 1, page_size: int = 10) -> Dict[str, Any]:
    offset = (page - 1) * page_size
    q = db.query(Transaction).filter(Transaction.user_id == user_id)
    total = q.count()
    items = q.order_by(Transaction.created_at.desc()).offset(offset).limit(page_size).all()
    return {
        "transactions": items,
        "total_count": total,
        "page": page,
        "page_size": page_size,
        "total_pages": max(1, (total + page_size - 1) // page_size),
    }


def get_pump_transactions(db: Session, pump_id: int, date_from: datetime, date_to: datetime) -> list:
    return db.query(Transaction).filter(
        Transaction.pump_id == pump_id,
        Transaction.status == TransactionStatus.COMPLETED,
        Transaction.completed_at >= date_from,
        Transaction.completed_at <= date_to,
    ).all()


def calculate_pump_settlement(db: Session, pump_id: int, date_from: datetime, date_to: datetime) -> Dict[str, Any]:
    transactions = get_pump_transactions(db, pump_id, date_from, date_to)
    total_amount = sum(t.amount for t in transactions)
    total_commission = sum(t.commission_amount for t in transactions)
    return {
        "total_transactions": len(transactions),
        "total_amount": total_amount,
        "commission_amount": total_commission,
        "net_amount": total_amount - total_commission,
        "transactions": transactions,
    }


def refund_transaction(db: Session, transaction_id: str, reason: str = "") -> Dict[str, Any]:
    transaction = db.query(Transaction).filter(
        Transaction.transaction_id == transaction_id,
        Transaction.status == TransactionStatus.COMPLETED,
    ).first()
    if not transaction:
        return {"success": False, "message": "Transaction not found or not eligible for refund"}
    try:
        credit_wallet(db, transaction.user_id, transaction.amount, TransactionType.REFUND)
        transaction.status = TransactionStatus.REFUNDED
        db.commit()
        logger.info("Refund processed: txn=%s amount=%.2f", transaction_id, transaction.amount)
        return {"success": True, "message": "Refund processed successfully", "refunded_amount": transaction.amount}
    except Exception as e:
        db.rollback()
        logger.error("Refund failed: %s", e)
        return {"success": False, "message": f"Refund failed: {e}"}


def get_wallet_summary(db: Session, user_id: int) -> Dict[str, Any]:
    wallet = get_or_create_wallet(db, user_id)
    recent = db.query(Transaction).filter(Transaction.user_id == user_id).order_by(Transaction.created_at.desc()).limit(5).all()
    month_start = datetime.utcnow().replace(day=1, hour=0, minute=0, second=0, microsecond=0)
    monthly = db.query(func.sum(Transaction.amount)).filter(
        Transaction.user_id == user_id,
        Transaction.transaction_type == TransactionType.FUEL_PURCHASE,
        Transaction.status == TransactionStatus.COMPLETED,
        Transaction.completed_at >= month_start,
    ).scalar() or 0.0
    return {
        "balance": wallet.balance,
        "total_recharged": wallet.total_recharged,
        "total_spent": wallet.total_spent,
        "monthly_spending": monthly,
        "recent_transactions": recent,
    }
