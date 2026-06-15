# Run guide (backend)

- Create and fill `.env` matching values in `creds` (or export env vars)
- Install deps and setup DB:
  - cd backend
  - pip install -r requirements.txt
  - python setup_db.py
- Start API:
  - uvicorn app.main:app --reload
- Start Celery (optional; for auto-recharge):
  - celery -A app.celery_app.celery_app worker --loglevel=info
  - celery -A app.celery_app.celery_app beat --loglevel=info 