#!/usr/bin/env python3
"""
Database setup script for ZapPay application.
This script creates the database tables and populates initial data.
"""

import sys
import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Add the app directory to Python path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.database import Base
from app.models import User, PetrolPump, UserRole
from app.services.auth import get_password_hash
from app.config import settings
import json

def create_tables():
    """Create all database tables."""
    engine = create_engine(settings.database_url)
    Base.metadata.create_all(bind=engine)
    print("✅ Database tables created successfully")

def create_admin_user():
    """Create initial admin user."""
    engine = create_engine(settings.database_url)
    SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    db = SessionLocal()
    
    try:
        # Check if admin user already exists
        admin_user = db.query(User).filter(
            User.phone_number == "9999999999",
            User.role == UserRole.ADMIN
        ).first()
        
        if admin_user:
            print("ℹ️  Admin user already exists")
            return
        
        # Create admin user
        admin_user = User(
            phone_number="9999999999",
            email="admin@zappay.com",
            full_name="System Administrator",
            password_hash=get_password_hash("admin123"),
            role=UserRole.ADMIN,
            is_active=True,
            is_verified=True
        )
        
        db.add(admin_user)
        db.commit()
        print("✅ Admin user created successfully")
        print("   Phone: 9999999999")
        print("   Password: admin123")
        
    except Exception as e:
        print(f"❌ Error creating admin user: {e}")
        db.rollback()
    finally:
        db.close()

def create_sample_pump():
    """Create a sample petrol pump for testing."""
    engine = create_engine(settings.database_url)
    SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    db = SessionLocal()
    
    try:
        # Check if sample pump already exists
        sample_pump = db.query(PetrolPump).filter(
            PetrolPump.license_number == "SAMPLE001"
        ).first()
        
        if sample_pump:
            print("ℹ️  Sample pump already exists")
            return
        
        # Create sample pump
        sample_pump = PetrolPump(
            pump_name="ZapPay Demo Pump",
            owner_name="Demo Owner",
            license_number="SAMPLE001",
            address="123 Demo Street, Test Area",
            city="Mumbai",
            state="Maharashtra",
            pincode="400001",
            phone_number="9876543210",
            email="demo@zappay.com",
            fuel_types=json.dumps(["petrol", "diesel"]),
            daily_fuel_capacity=10000.0,
            latitude=19.0760,
            longitude=72.8777,
            is_active=True,
            is_verified=True
        )
        
        db.add(sample_pump)
        db.commit()
        print("✅ Sample pump created successfully")
        
    except Exception as e:
        print(f"❌ Error creating sample pump: {e}")
        db.rollback()
    finally:
        db.close()

def create_test_users():
    """Create test users for development."""
    engine = create_engine(settings.database_url)
    SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    db = SessionLocal()
    
    test_users = [
        {
            "phone_number": "9876543210",
            "email": "customer@zappay.com",
            "full_name": "Test Customer",
            "role": UserRole.CUSTOMER,
            "vehicle_number": "MH01AB1234"
        },
        {
            "phone_number": "9876543211",
            "email": "owner@zappay.com",
            "full_name": "Test Pump Owner",
            "role": UserRole.PUMP_OWNER
        },
        {
            "phone_number": "9876543212",
            "email": "operator@zappay.com",
            "full_name": "Test Pump Operator",
            "role": UserRole.PUMP_OPERATOR
        }
    ]
    
    try:
        for user_data in test_users:
            # Check if user already exists
            existing_user = db.query(User).filter(
                User.phone_number == user_data["phone_number"]
            ).first()
            
            if existing_user:
                continue
            
            # Create user
            user = User(
                phone_number=user_data["phone_number"],
                email=user_data["email"],
                full_name=user_data["full_name"],
                password_hash=get_password_hash("test123"),
                role=user_data["role"],
                is_active=True,
                is_verified=True,
                vehicle_number=user_data.get("vehicle_number")
            )
            
            db.add(user)
        
        db.commit()
        print("✅ Test users created successfully")
        print("   All test users have password: test123")
        
    except Exception as e:
        print(f"❌ Error creating test users: {e}")
        db.rollback()
    finally:
        db.close()

def main():
    """Main setup function."""
    print("🚀 Setting up ZapPay database...")
    print("=" * 50)
    
    try:
        # Create tables
        create_tables()
        
        # Create initial data
        create_admin_user()
        create_sample_pump()
        
        # Create test users (only in debug mode)
        if settings.debug:
            create_test_users()
        
        print("=" * 50)
        print("✅ Database setup completed successfully!")
        print("\nYou can now start the application with:")
        print("   python -m uvicorn app.main:app --reload")
        
    except Exception as e:
        print(f"❌ Database setup failed: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main() 