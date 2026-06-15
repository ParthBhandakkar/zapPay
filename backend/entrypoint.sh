#!/bin/bash
# Render entrypoint — respects Render's PORT env var
set -e

PORT="${PORT:-8000}"
WORKERS="${WORKERS:-1}"  # 1 worker on Render free tier (512MB, EasyOCR/Torch is heavy)

echo "Starting ZapPay API on port $PORT with $WORKERS workers"

exec gunicorn app.main:app \
    --worker-class uvicorn.workers.UvicornWorker \
    --workers "$WORKERS" \
    --bind "0.0.0.0:$PORT" \
    --max-requests 10000 \
    --max-requests-jitter 1000 \
    --timeout 120 \
    --keep-alive 5 \
    --log-level info \
    --access-logfile -
