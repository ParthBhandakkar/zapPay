from sqlalchemy.orm import Session
from app.database import SessionLocal
from app.models import PetrolPump, PumpOperator
from app.schemas import PetrolPumpResponse
import json

def inspect_pump():
    db = SessionLocal()
    try:
        print("Inspecting Petrol Pumps...")
        pumps = db.query(PetrolPump).all()
        for pump in pumps:
            print(f"ID: {pump.id}")
            print(f"Name: {pump.pump_name}")
            print(f"Email: '{pump.email}' (Type: {type(pump.email)})")
            print(f"Fuel Types: '{pump.fuel_types}' (Type: {type(pump.fuel_types)})")
            print(f"Commission Rate: {pump.commission_rate} (Type: {type(pump.commission_rate)})")
            print(f"Lat/Lon: {pump.latitude}/{pump.longitude}")
            print(f"Created At: {pump.created_at} (Type: {type(pump.created_at)})")
            
            # Try to validate with Pydantic
            try:
                model = PetrolPumpResponse.from_orm(pump)
                print("Pydantic Validation: SUCCESS")
                print(model.json())
            except Exception as e:
                print(f"Pydantic Validation: FAILED")
                print(e)
                
    except Exception as e:
        print(f"Error: {e}")
    finally:
        db.close()

if __name__ == "__main__":
    inspect_pump()
