# QR Scanner Setup for Mobile Testing

## Problem
Camera access requires HTTPS or localhost. When accessing via IP address (http://192.168.29.181:5173), the camera won't work.

## Solution 1: ngrok (Recommended - Easiest)

### Step 1: Install ngrok
1. Download from: https://ngrok.com/download
2. Extract and place ngrok.exe in a folder
3. Sign up for free account at https://dashboard.ngrok.com/signup
4. Get your auth token from dashboard

### Step 2: Setup ngrok
```powershell
# In PowerShell, navigate to ngrok folder
cd path\to\ngrok

# Authenticate (one-time setup)
.\ngrok.exe authtoken YOUR_AUTH_TOKEN

# Start tunnel for Vite dev server
.\ngrok.exe http 5173
```

### Step 3: Use the HTTPS URL
ngrok will show something like:
```
Forwarding    https://abc123.ngrok-free.app -> http://localhost:5173
```

**Open that HTTPS URL on your phone** - Camera will work! ✅

---

## Solution 2: Vite HTTPS (Self-Signed Certificate)

### Step 1: Install mkcert
```powershell
# Using chocolatey
choco install mkcert

# OR download from: https://github.com/FiloSottile/mkcert/releases
```

### Step 2: Generate Certificates
```powershell
# Create local CA
mkcert -install

# Generate certificates
cd D:\zapPay\backend\frontend-react
mkcert localhost 192.168.29.181
```

### Step 3: Update vite.config.js
```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import fs from 'fs'

export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
    https: {
      key: fs.readFileSync('./localhost+1-key.pem'),
      cert: fs.readFileSync('./localhost+1.pem'),
    },
    cors: true
  }
})
```

### Step 4: Access via HTTPS
Open: `https://192.168.29.181:5173` on your phone
(You may need to accept the self-signed certificate warning)

---

## Solution 3: Manual QR Input (Temporary Testing)

If you just want to test the flow without camera, I can add a manual QR input field.

---

## Recommendation

**Use ngrok** - It's the quickest and most reliable for testing:
- ✅ No certificate setup needed
- ✅ Works immediately
- ✅ Free for testing
- ✅ Real HTTPS (no warnings)
- ✅ Shareable URL

Just run:
```powershell
ngrok http 5173
```

Then use the https URL on any device!

---

## Alternative: Deploy to Production

For permanent solution, deploy your app to:
- **Vercel** (free, auto HTTPS)
- **Netlify** (free, auto HTTPS)
- **Railway** (free tier)

All provide automatic HTTPS certificates.


