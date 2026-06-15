import os
import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from fastapi.testclient import TestClient

# Override settings before importing app
os.environ["DATABASE_URL"] = "sqlite:///./test_zappay.db"
os.environ["SECRET_KEY"] = "test-secret-key-for-testing-only-12345678"
os.environ["DEBUG"] = "true"
os.environ["REDIS_URL"] = "redis://localhost:6379/0"
os.environ["RATE_LIMIT_ENABLED"] = "false"

from app.database import Base, get_db
from app.main import app


@pytest.fixture(scope="session")
def db_engine():
    engine = create_engine("sqlite:///./test_zappay.db", connect_args={"check_same_thread": False})
    Base.metadata.create_all(bind=engine)
    yield engine
    Base.metadata.drop_all(bind=engine)
    os.unlink("test_zappay.db")


@pytest.fixture
def db_session(db_engine):
    connection = db_engine.connect()
    transaction = connection.begin()
    Session = sessionmaker(bind=connection)
    session = Session()
    yield session
    session.close()
    transaction.rollback()
    connection.close()


@pytest.fixture
def client(db_session):
    def override_get_db():
        yield db_session

    app.dependency_overrides[get_db] = override_get_db
    yield TestClient(app)
    app.dependency_overrides.clear()
