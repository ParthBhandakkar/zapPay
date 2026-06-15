# Environment setup

- Copy the `creds` file values into `backend/.env` or export as environment variables
- The app loads `.env` from both `backend/.env` and repo root `.env` if present
- Minimal required keys to run locally:
  - SECRET_KEY
  - DATABASE_URL
  - REDIS_URL

- Optional for full functionality:
  - RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET
  - STRIPE_SECRET_KEY, STRIPE_WEBHOOK_SECRET
  - QR_ENCRYPTION_KEY (Fernet key)
  - SMTP_* or SMS_API_KEY for notifications 