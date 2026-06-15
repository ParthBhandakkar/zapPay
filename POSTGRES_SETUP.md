# PostgreSQL Setup Guide for ZapPay

This guide will help you set up a production-ready PostgreSQL database for ZapPay on your Windows machine.

## 1. Install PostgreSQL

1.  **Download**: Go to [postgresql.org/download/windows/](https://www.postgresql.org/download/windows/) and download the installer for the latest version (e.g., PostgreSQL 16).
2.  **Install**: Run the installer.
    *   **Password**: When asked for a password for the database superuser (`postgres`), enter a strong password and **remember it**. (e.g., `admin123` for development, but use something stronger for production).
    *   **Port**: Keep the default port `5432`.
    *   **Locale**: Default is fine.
3.  **Finish**: Complete the installation. You can uncheck "Stack Builder" at the end.

## 2. Create the Database and User

1.  Open the **pgAdmin 4** application (installed with PostgreSQL) OR use the SQL Shell (psql).
2.  **Using SQL Shell (psql)**:
    *   Search for "SQL Shell (psql)" in Windows Start menu.
    *   Press Enter for Server, Database, Port, Username (defaults).
    *   Enter the password you set during installation.
    *   Run the following commands:

```sql
-- Create a dedicated user for ZapPay
CREATE USER zappay_user WITH PASSWORD 'password';

-- Create the database
CREATE DATABASE zappay_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE zappay_db TO zappay_user;

-- Connect to the database to grant schema privileges (optional but recommended)
\c zappay_db
GRANT ALL ON SCHEMA public TO zappay_user;
```

## 3. Configure ZapPay Backend

1.  Open `backend/.env` file.
2.  Comment out the SQLite line and uncomment/update the PostgreSQL line:

```ini
# DATABASE_URL=sqlite:///./zappay.db
DATABASE_URL=postgresql://zappay_user:password@localhost:5432/zappay_db
```

*Note: If you used a different password or username, update the URL accordingly.*

## 4. Initialize Database Tables

Once PostgreSQL is running and configured, run the migration setup script (I will provide this).

1.  Open a terminal in `D:\zapPay\backend`.
2.  Run: `python init_alembic.py` (This will set up the migration environment).
3.  Run: `alembic revision --autogenerate -m "Initial migration"` (This creates the migration file).
4.  Run: `alembic upgrade head` (This applies the migration to the database).

## 5. Verify

You can now run the backend server:
```bash
python -m uvicorn app.main:app --reload
```
The application will now use the PostgreSQL database.
