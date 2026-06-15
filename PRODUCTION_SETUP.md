# ZapPay - Production Setup Guide

## Overview
ZapPay is a QR-based fuel payment platform. This guide covers setting up the application for production with PostgreSQL and Redis.

## Prerequisites

- Python 3.10+
- Node.js 18+
- PostgreSQL 14+
- Redis 6+
- Git

## Backend Setup

### 1. Clone and Navigate
```bash
cd backend
```

### 2. Create Virtual Environment
```bash
python -m venv venv

# Windows
.\venv\Scripts\activate

# Linux/Mac
source venv/bin/activate
```

### 3. Install Dependencies
```bash
pip install -r requirements.txt
```

### 4. Configure Environment Variables

Copy the example env file and configure it:
```bash
cp .env.example .env
```

Edit `.env` with your production values:

```env
# IMPORTANT: Generate a strong secret key
SECRET_KEY=your-generated-secret-key-minimum-64-characters

# PostgreSQL Database (required for production)
DATABASE_URL=postgresql://username:password@localhost:5432/zappay_db

# Redis (required for caching and pump settings)
REDIS_URL=redis://localhost:6379/0

# Set to false for production
DEBUG=false

# Payment Gateways (get keys from respective dashboards)
RAZORPAY_KEY_ID=rzp_live_xxxxxxxxxxxx
RAZORPAY_KEY_SECRET=your_razorpay_secret

STRIPE_SECRET_KEY=sk_live_xxxxxxxxxxxx
STRIPE_WEBHOOK_SECRET=whsec_xxxxxxxxxxxx

# QR Code encryption (generate using Fernet)
QR_ENCRYPTION_KEY=your-fernet-key

# CORS - Add your frontend domain
CORS_ORIGINS=https://your-frontend-domain.com,https://your-pump-app-domain.com
```

### 5. Generate Security Keys

```python
# Generate SECRET_KEY
python -c "import secrets; print(secrets.token_urlsafe(64))"

# Generate QR_ENCRYPTION_KEY (Fernet key)
python -c "from cryptography.fernet import Fernet; print(Fernet.generate_key().decode())"
```

### 6. Setup PostgreSQL Database

```sql
-- Connect to PostgreSQL and create database
CREATE DATABASE zappay_db;
CREATE USER zappay_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE zappay_db TO zappay_user;
```

### 7. Initialize Database

```bash
python setup_db.py
```

This creates:
- All database tables
- Admin user (phone: 9999999999, password: admin123)
- Sample petrol pump for testing

### 8. Start the Backend Servers

**Customer API (Port 8000):**
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

**Pump API (Port 8001):**
```bash
uvicorn app.pump_app:app --host 0.0.0.0 --port 8001
```

For production, use Gunicorn:
```bash
gunicorn app.main:app -w 4 -k uvicorn.workers.UvicornWorker --bind 0.0.0.0:8000
gunicorn app.pump_app:app -w 4 -k uvicorn.workers.UvicornWorker --bind 0.0.0.0:8001
```

## Frontend Setup

### 1. Navigate to Frontend
```bash
cd frontend/ZapPay
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Configure Environment

Create `.env` file:
```bash
cp .env.example .env
```

Edit `.env`:
```env
# API URLs (update for production)
VITE_API_CUSTOMER_URL=https://api.your-domain.com/api/v1
VITE_API_PUMP_URL=https://pump-api.your-domain.com

# Disable demo mode for production
VITE_DEMO_MODE=false

# Payment gateway public keys
VITE_RAZORPAY_KEY_ID=rzp_live_xxxxxxxxxxxx
VITE_STRIPE_PUBLISHABLE_KEY=pk_live_xxxxxxxxxxxx
```

### 4. Build for Production
```bash
npm run build
```

### 5. Deploy
The build output is in `dist/` folder. Deploy to your web server or CDN.

## Redis Setup

### Windows (using Windows Subsystem for Linux)
```bash
wsl
sudo apt update
sudo apt install redis-server
sudo service redis-server start
```

### Linux
```bash
sudo apt install redis-server
sudo systemctl enable redis-server
sudo systemctl start redis-server
```

### Verify Redis
```bash
redis-cli ping
# Should respond: PONG
```

## API Endpoints

### Health Checks
- Customer API: `GET http://localhost:8000/health`
- Pump API: `GET http://localhost:8001/health`

### API Documentation (Debug mode only)
- Customer API Docs: `http://localhost:8000/docs`
- Pump API Docs: `http://localhost:8001/docs`

## Test Credentials (Debug Mode)

| Role | Phone | Password |
|------|-------|----------|
| Admin | 9999999999 | admin123 |
| Customer | 9876543210 | test123 |
| Pump Owner | 9876543211 | test123 |
| Pump Operator | 9876543212 | test123 |

## Production Checklist

- [ ] Set `DEBUG=false`
- [ ] Use strong `SECRET_KEY` (64+ characters)
- [ ] Configure PostgreSQL (not SQLite)
- [ ] Configure Redis with password
- [ ] Set up HTTPS/SSL
- [ ] Configure CORS origins properly
- [ ] Set up proper logging
- [ ] Configure payment gateway production keys
- [ ] Set up database backups
- [ ] Configure rate limiting
- [ ] Set up monitoring (e.g., Sentry, Prometheus)

## Architecture

```
┌─────────────────┐     ┌─────────────────┐
│   Customer App  │     │    Pump App     │
│   (React/Vite)  │     │   (React/Vite)  │
└────────┬────────┘     └────────┬────────┘
         │                       │
         ▼                       ▼
┌─────────────────┐     ┌─────────────────┐
│  Customer API   │     │    Pump API     │
│   (Port 8000)   │     │   (Port 8001)   │
└────────┬────────┘     └────────┬────────┘
         │                       │
         └───────────┬───────────┘
                     │
         ┌───────────┼───────────┐
         ▼           ▼           ▼
   ┌──────────┐ ┌──────────┐ ┌──────────┐
   │PostgreSQL│ │  Redis   │ │ Payment  │
   │ Database │ │  Cache   │ │ Gateway  │
   └──────────┘ └──────────┘ └──────────┘
```

## Troubleshooting

### Database Connection Error
```
Check that PostgreSQL is running and credentials are correct:
psql -U zappay_user -d zappay_db -h localhost
```

### Redis Connection Error
```
Check Redis is running:
redis-cli ping
```

### CORS Errors
Ensure your frontend domain is in `CORS_ORIGINS` in backend `.env`

### Payment Gateway Errors
Verify API keys are correct and match the environment (test vs production)
