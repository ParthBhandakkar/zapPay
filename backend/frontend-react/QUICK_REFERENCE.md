# ZapPay React - Quick Reference Card

## 🚀 Start Everything (3 Terminals)

```bash
# Terminal 1 - Customer API
cd backend
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Terminal 2 - Pump API  
cd backend
python -m uvicorn app.pump_app:app --reload --host 0.0.0.0 --port 8001

# Terminal 3 - React Frontend
cd backend/frontend-react
npm run dev
```

## 🌐 Access URLs

| Device | URL |
|--------|-----|
| Your Computer | http://localhost:5173 |
| Phone (Same WiFi) | http://192.168.29.181:5173 |
| Find Your IP | `ipconfig` (Windows) or `ifconfig` (Mac/Linux) |

## 👤 Customer Quick Test

1. Select "Customer"
2. Signup → Name: `Test User`, Phone: `1234567890`, Password: `test123`
3. Send OTP → Check terminal for OTP code
4. Complete Signup
5. Generate QR → Mobile type → Show on screen

## ⛽ Pump Quick Test

1. Select "Pump Operator"
2. Signup → Name: `Pump Owner`, Phone: `0987654321`, Password: `test123`
3. Send OTP → Check terminal for OTP code
4. Complete Signup
5. Settings → Pump ID: `1`, Name: `Test Pump`, Price: `100` → Save
6. Scanner → Start → Scan customer QR
7. Enter fuel details → Complete Purchase

## 📱 Test OTP Codes

Check backend terminal for lines like:
```
SMS OTP for 1234567890: 123456
```

## 🔑 Default Test Data

- Pump ID: `1` (from setup_db.py)
- Test recharge in Wallet (dev only)
- Password required for signup (✅ FIXED!)

## 🐛 Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| Can't access from phone | Check same WiFi, use IP not localhost |
| 401 Errors | Backend servers running? Check terminals |
| Camera not working | Grant permissions, use HTTPS in production |
| OTP not showing | Check backend terminal output |

## 📦 NPM Commands

```bash
npm run dev          # Start development server
npm run build        # Build for production
npm run preview      # Preview production build
```

## 🎯 Key Features Checklist

- [x] Password in signup
- [x] OTP signup/login
- [x] Customer features (7 pages)
- [x] Pump features (6 pages)
- [x] QR generation
- [x] QR scanning
- [x] Fuel purchase
- [x] Wallet management
- [x] Transaction history
- [x] Network access (no ngrok!)

## 🎨 UI Theme

- Customer: Purple gradient (#667eea → #764ba2)
- Pump: Pink gradient (#f093fb → #f5576c)
- Cards: White with shadows
- Buttons: Gradient with hover effects

## 📞 Support

Check these files for help:
- `SETUP_GUIDE.md` - Detailed setup
- `COMPLETE.md` - Full feature list
- `README_REACT.md` - Architecture details

---

**Everything works! Just run the 3 terminals and access the URL!** 🚀

