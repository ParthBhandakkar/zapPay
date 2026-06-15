from sqlalchemy.orm import Session
from app.database import SessionLocal, engine
from app.models import PetrolPump, PumpOperator, Transaction, Settlement

def clean_pumps():
    db = SessionLocal()
    try:
        print("Cleaning database records for Petrol Pumps...")
        
        # 1. Delete Settlements
        deleted_settlements = db.query(Settlement).delete()
        print(f"Deleted {deleted_settlements} settlements.")
        
        # 2. Delete Transactions linked to pumps
        # Note: This deletes ALL transactions linked to a pump. 
        # If you want to keep wallet recharges (which might not have a pump_id), filter by pump_id is not None
        deleted_transactions = db.query(Transaction).filter(Transaction.pump_id.isnot(None)).delete(synchronize_session=False)
        print(f"Deleted {deleted_transactions} pump transactions.")
        
        # 3. Delete Pump Operators
        deleted_operators = db.query(PumpOperator).delete()
        print(f"Deleted {deleted_operators} pump operators.")
        
        # 4. Delete Petrol Pumps
        deleted_pumps = db.query(PetrolPump).delete()
        print(f"Deleted {deleted_pumps} petrol pumps.")
        
        db.commit()
        print("Successfully cleaned all pump records.")
        
    except Exception as e:
        print(f"Error cleaning pumps: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    clean_pumps()
