@echo off
echo ============================================
echo   Starting ZapPay - Unified Server
echo ============================================
echo.
echo Starting Unified API (Port 8000)...
echo This server handles both Customer and Pump requests.
echo.

start "ZapPay Unified API" cmd /k "cd /d %~dp0 && python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000"

echo.
echo ============================================
echo   Server started at http://localhost:8000
echo ============================================
echo.
echo Next steps:
echo   1. Start your single ngrok tunnel:
echo      ngrok http 8000
echo.
echo   2. Update mobile/.env with the ngrok URL for BOTH APIs:
echo      EXPO_PUBLIC_CUSTOMER_API=https://...
echo      EXPO_PUBLIC_PUMP_API=https://...
echo.
pause
