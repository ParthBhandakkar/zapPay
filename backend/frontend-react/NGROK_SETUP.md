# 🚀 ngrok Setup Guide for Mobile Testing

## Problem
When accessing the app via ngrok (HTTPS), the backend APIs are still on HTTP, causing "Mixed Content" errors in browsers.

## Solution: Run 3 ngrok Tunnels

You need to expose all 3 services via ngrok:

### 1️⃣ **Frontend** (React App - Port 5173)
```bash
ngrok http 5173
```
Example output: `https://abc123.ngrok-free.app`

### 2️⃣ **Customer API** (Backend - Port 8000)
```bash
ngrok http 8000
```
Example output: `https://xyz456.ngrok-free.app`

### 3️⃣ **Pump API** (Backend - Port 8001)
```bash
ngrok http 8001
```
Example output: `https://def789.ngrok-free.app`

---

## 📝 Configuration Steps

### Step 1: Start all 3 ngrok tunnels
Open **3 separate terminals** and run:

**Terminal 1:**
```bash
ngrok http 5173
```

**Terminal 2:**
```bash
ngrok http 8000
```

**Terminal 3:**
```bash
ngrok http 8001
```

### Step 2: Copy the ngrok URLs
You'll get 3 HTTPS URLs, something like:
- Frontend: `https://0304b8666e73.ngrok-free.app`
- Customer API: `https://a1b2c3d4e5f6.ngrok-free.app`
- Pump API: `https://1a2b3c4d5e6f.ngrok-free.app`

### Step 3: Create `.env.local` file
In `backend/frontend-react/`, create a file named `.env.local`:

```env
VITE_CUSTOMER_API=https://a1b2c3d4e5f6.ngrok-free.app
VITE_PUMP_API=https://1a2b3c4d5e6f.ngrok-free.app
```

Replace with your actual ngrok URLs (without `/api/v1` suffix).

### Step 4: Restart the React dev server
```bash
cd backend/frontend-react
npm run dev
```

### Step 5: Open the frontend ngrok URL on your phone
```
https://0304b8666e73.ngrok-free.app
```

---

## ✅ Verification

Open your browser console. You should see:
- ✅ No "Mixed Content" errors
- ✅ API calls going to HTTPS endpoints
- ✅ Camera access working on mobile

---

## 🎯 Alternative: ngrok Paid Plan (Easier)

If you have an ngrok paid plan, use the config file approach:

### Create `ngrok.yml`:
```yaml
version: "2"
authtoken: YOUR_AUTH_TOKEN

tunnels:
  frontend:
    proto: http
    addr: 5173
    domain: zappay-frontend.ngrok.io

  customer-api:
    proto: http
    addr: 8000
    domain: zappay-customer.ngrok.io

  pump-api:
    proto: http
    addr: 8001
    domain: zappay-pump.ngrok.io
```

### Start all tunnels:
```bash
ngrok start --all
```

---

## 🔧 Troubleshooting

### Problem: "net::ERR_CONNECTION_REFUSED"
**Solution:** Make sure all 3 servers are running:
```bash
# Terminal 1: Customer API
cd backend
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Terminal 2: Pump API
cd backend
python -m uvicorn app.pump_app:app --reload --host 0.0.0.0 --port 8001

# Terminal 3: React Frontend
cd backend/frontend-react
npm run dev
```

### Problem: "Invalid Host header"
**Solution:** Already fixed in `vite.config.js` with `allowedHosts`.

### Problem: ngrok session expired
**Solution:** Free ngrok tunnels expire after 2 hours. Restart and update `.env.local` with new URLs.

---

## 💡 Quick Test (Without ngrok)

For local testing without camera:
1. Open `http://localhost:5173` on your computer
2. Use the **Manual QR Input** field in the Scanner page
3. Copy QR data from the Customer interface console

---

## 📱 Production Deployment

For production, deploy to a proper HTTPS hosting:
- **Frontend:** Vercel, Netlify, Cloudflare Pages
- **Backend:** Railway, Render, Heroku (all provide free HTTPS)

Then camera will work without ngrok! 🎉

