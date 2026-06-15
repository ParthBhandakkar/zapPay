@echo off
echo ============================================
echo   ZapPay Database Migration Setup
echo ============================================
echo.
echo This script will help you run the initial database migration.
echo.
echo PREREQUISITES:
echo 1. PostgreSQL must be installed and running.
echo 2. Database 'zappay_db' must exist.
echo 3. backend/.env must have the correct DATABASE_URL.
echo.
pause

cd backend

echo.
echo [1/2] Generating migration file...
alembic revision --autogenerate -m "Initial migration"

echo.
echo [2/2] Applying migration to database...
alembic upgrade head

echo.
echo ============================================
echo   Migration Completed!
echo ============================================
echo.
pause
