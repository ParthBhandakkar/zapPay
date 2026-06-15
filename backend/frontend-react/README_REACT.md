# ZapPay React Frontend

A modern React frontend for the ZapPay fuel payment system with separate customer and pump operator interfaces.

## Features

### Customer Interface
- OTP-based signup and login with password
- Dashboard with wallet summary and recent transactions
- QR code generation (mobile/sticker types)
- Profile management
- KYC submission and status check
- Wallet recharge (test mode)
- Transaction history
- Real-time WebSocket notifications

### Pump Operator Interface
- OTP-based signup and login with password
- Pump settings management (Redis-backed)
- QR code scanner with camera
- Fuel purchase processing
- Transaction history
- Operator management
- Dashboard with KPIs

## Setup

1. **Install dependencies:**
   ```bash
   cd backend/frontend-react
   npm install
   ```

2. **Configure API endpoints:**
   Create `.env` file:
   ```
   VITE_CUSTOMER_API=http://localhost:8000
   VITE_PUMP_API=http://localhost:8001
   ```

3. **Run development server:**
   ```bash
   npm run dev
   ```
   Access at: http://localhost:5173

4. **Build for production:**
   ```bash
   npm run build
   ```

## Project Structure

```
src/
├── components/
│   ├── RoleSelector.jsx          # Initial role selection screen
│   ├── Toast.jsx                  # Toast notifications
│   └── Loader.jsx                 # Loading spinner
├── pages/
│   ├── Customer/
│   │   ├── CustomerApp.jsx        # Main customer app
│   │   ├── Dashboard.jsx          # Customer dashboard
│   │   ├── Auth.jsx               # Signup/Login
│   │   ├── Wallet.jsx             # Wallet management
│   │   ├── QRGenerate.jsx         # QR generation
│   │   ├── Profile.jsx            # Profile view/edit
│   │   ├── KYC.jsx                # KYC submission
│   │   └── Transactions.jsx       # Transaction history
│   └── Pump/
│       ├── PumpApp.jsx            # Main pump app
│       ├── Dashboard.jsx          # Pump dashboard
│       ├── Auth.jsx               # Signup/Login
│       ├── Settings.jsx           # Pump settings
│       ├── Scanner.jsx            # QR scanner
│       ├── Transactions.jsx       # Transaction history
│       └── Operators.jsx          # Operator management
├── utils/
│   ├── api.js                     # Axios instances & interceptors
│   └── websocket.js               # WebSocket connection
├── App.jsx                        # Main app router
└── main.jsx                       # Entry point
```

## Key Improvements over HTML/JS version

1. **No ngrok needed for local network**: React dev server with `--host` flag allows access from any device on the same network
2. **Component-based architecture**: Reusable, maintainable code
3. **Proper state management**: React hooks for clean state handling
4. **Better routing**: React Router for navigation
5. **Modern UI**: Clean, responsive design with smooth animations
6. **Type safety ready**: Easy to migrate to TypeScript
7. **Better error handling**: Comprehensive error boundaries and user feedback
8. **Real-time updates**: WebSocket integration with automatic reconnection

## Running on Local Network

To access from mobile on same WiFi:

```bash
npm run dev -- --host
```

Then access via: `http://<YOUR_IP>:5173`

## Environment Variables

- `VITE_CUSTOMER_API`: Customer API base URL (default: http://localhost:8000)
- `VITE_PUMP_API`: Pump API base URL (default: http://localhost:8001)

## Tech Stack

- **React 18**: UI library
- **Vite**: Build tool and dev server
- **React Router**: Client-side routing
- **Axios**: HTTP client with interceptors
- **html5-qrcode**: QR code scanning
- **CSS3**: Modern styling with flexbox/grid

## Notes

- Password field now required during signup (fixes the login issue)
- Supports both OTP and password login
- Auto token refresh on 401 errors
- LocalStorage for token persistence
- Mobile-responsive design
- Offline-ready architecture

