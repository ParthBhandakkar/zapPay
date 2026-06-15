@echo off
echo ============================================
echo   Starting ZapPay - All Services
echo ============================================
echo.

echo [1/3] Starting Customer API (Port 8000)...
start "ZapPay Customer API" cmd /k "cd /d %~dp0 && python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000"
timeout /t 2 /nobreak >nul

echo [2/3] Starting Pump API (Port 8001)...
start "ZapPay Pump API" cmd /k "cd /d %~dp0 && python -m uvicorn app.pump_app:app --reload --host 0.0.0.0 --port 8001"
timeout /t 2 /nobreak >nul

echo [3/3] Starting React Frontend (Port 5173)...
start "ZapPay Frontend" cmd /k "cd /d %~dp0\frontend-react && npm run dev"

echo.
echo ============================================
echo   All servers started!
echo ============================================
echo.
echo   Customer API: http://localhost:8000
echo   Pump API:     http://localhost:8001
echo   Frontend:     http://localhost:5173
echo.
echo   Next: Start 3 ngrok tunnels (see NGROK_SETUP.md)
echo ============================================
pause

