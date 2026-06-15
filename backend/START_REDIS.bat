@echo off
echo Starting Redis Server...
echo If Redis is not installed, download from: https://redis.io/download
echo Or install via: choco install redis-64
echo.

redis-server

echo.
echo Redis server stopped.
pause
