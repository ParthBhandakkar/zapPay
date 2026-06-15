@echo off
echo ============================================
echo   Starting ngrok Tunnels for ZapPay
echo ============================================
echo.
echo This will open an ngrok tunnel for the Unified Backend (port 8000).
echo.
echo IMPORTANT: Copy the HTTPS URL and update:
echo   1. mobile/.env (EXPO_PUBLIC_CUSTOMER_API and EXPO_PUBLIC_PUMP_API)
echo   2. backend/frontend-react/.env.local (VITE_CUSTOMER_API)
echo.
pause

start "ngrok - Unified Backend 8000" cmd /k "ngrok http 8000"

echo.
echo ============================================
echo   Ngrok tunnel started!
echo ============================================
echo.
pause

