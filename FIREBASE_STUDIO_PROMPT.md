# 🚀 ZapPay Mobile App - Complete Development Prompt for Firebase Studio

## 📱 App Overview

**ZapPay** is a digital wallet and QR code-based fuel payment system that allows customers to pay for fuel at petrol pumps using their mobile phones. The app has two main user roles: **Customers** and **Pump Operators/Owners**.

### Core Concept
- Customers generate QR codes from their wallet balance
- Pump operators scan QR codes to process fuel purchases
- Real-time transaction processing with instant wallet deduction
- Secure, encrypted QR codes with expiration
- SMS notifications for all transactions

---

## 🎨 Design Requirements

### Color Scheme
- **Primary Brand Color**: Purple gradient (`#6B46C1` to `#9333EA`)
- **Secondary Colors**: 
  - Success/Green: `#10B981`
  - Warning/Orange: `#F59E0B`
  - Error/Red: `#EF4444`
  - Info/Blue: `#3B82F6`
- **Background**: Light grey (`#F3F4F6`) with white cards
- **Text**: Dark grey (`#1F2937`) for headings, `#6B7280` for body

### UI/UX Principles
1. **Modern & Clean**: Material Design 3 principles with rounded corners (12px radius)
2. **Mobile-First**: Optimized for phones (primary) and tablets
3. **Intuitive Navigation**: Bottom tab bar for main sections, top navigation for sub-pages
4. **Smooth Animations**: Subtle transitions (200-300ms) for state changes
5. **Accessibility**: Large touch targets (min 44x44px), clear contrast ratios
6. **Loading States**: Skeleton loaders or spinners for async operations
7. **Error Handling**: User-friendly error messages with retry options

### Typography
- **Headings**: Inter or Roboto Bold, 24-32px
- **Body**: Inter or Roboto Regular, 14-16px
- **Labels**: Inter Medium, 12-14px
- **Monospace**: For transaction IDs and amounts

---

## 🔐 Authentication System

### Backend API Endpoints

**Base URLs:**
- Customer API: `http://localhost:8000/api/v1`
- Pump API: `http://localhost:8001`

**Authentication Flow:**
1. **OTP Registration** → **Password Setup** → **Login**
2. JWT tokens stored in secure storage (access + refresh tokens)
3. Auto token refresh on 401 errors
4. Role-based access control

### API Endpoints

#### Authentication (`/api/v1/auth`)
```
POST /auth/register
Body: {
  phone_number: string,
  full_name: string,
  password: string,
  confirm_password: string,
  role: "customer" | "pump_owner"
}
Response: { success: true, message: "OTP sent", data: { user_id } }

POST /auth/verify-otp
Body: { phone_number: string, otp: string }
Response: { success: true, message: "OTP verified" }

POST /auth/login
Body: { phone_number: string, password: string }
Response: {
  access_token: string,
  refresh_token: string,
  token_type: "bearer",
  expires_in: 1800
}

POST /auth/refresh
Body: { refresh_token: string }
Response: { access_token: string, refresh_token: string }
```

**Headers Required:**
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

---

## 👤 Customer App Features

### 1. **Role Selection Screen**
- Two large cards: "Customer" and "Pump Operator"
- Gradient backgrounds (purple for customer, blue for pump)
- Icon + title + description
- Smooth transition to selected role

### 2. **Authentication Screens**
- **Sign Up Flow:**
  1. Phone number input with country code (+91)
  2. Full name input
  3. Password + confirm password
  4. OTP verification screen (6-digit input)
  5. Success → Auto login

- **Login Screen:**
  - Phone number + password
  - "Forgot Password?" link
  - "Sign Up" link

### 3. **Dashboard** (`/customer/dashboard`)
**API:** `GET /wallet/summary` (requires auth)

**Display:**
- **Wallet Balance Card** (large, prominent)
  - Balance amount (₹X,XXX.XX)
  - "Recharge Wallet" button
  
- **Quick Stats Cards** (3 columns):
  - Total Spent (this month)
  - Transactions Count
  - Last Transaction Date

- **Recent Transactions** (last 3-5)
  - Transaction type icon
  - Amount (+/-)
  - Date/time
  - Tap to view details

### 4. **Wallet** (`/customer/wallet`)
**API:** `GET /wallet/balance` (requires auth)

**Features:**
- Current balance display
- Recharge button → Opens payment gateway
- Transaction history link
- Auto-recharge toggle (if implemented)

**Recharge Flow:**
```
POST /wallet/recharge
Body: { amount: number, payment_method: "razorpay" | "stripe" }
Response: { success: true, payment_url: string }
```

### 5. **QR Code Generation** (`/customer/qr`)
**API:** `POST /qr/generate` (requires auth)

**Features:**
- QR type selector: "Mobile" or "Sticker"
- Generate button
- Display generated QR code (large, scannable)
- Download/share options
- QR expiration timer
- List of active QR codes

**Request:**
```json
{
  "qr_type": "mobile" | "sticker",
  "expires_in_minutes": 30
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "qr_code_id": 123,
    "qr_code": "encrypted_string",
    "qr_image_url": "http://.../qr/123/image",
    "expires_at": "2025-11-16T10:30:00Z"
  }
}
```

### 6. **Profile** (`/customer/profile`)
**API:** `GET /users/profile` (requires auth)

**Display:**
- Profile picture (placeholder/avatar)
- Full name (editable)
- Phone number (read-only)
- Email (editable)
- Address fields (editable)
- Vehicle info (number, type)
- KYC status badge

**Update API:** `PUT /users/profile`

### 7. **KYC** (`/customer/kyc`)
**API:** `GET /users/kyc/status` (requires auth)

**Features:**
- Status display (Pending/Verified/Rejected)
- Form for submission:
  - Aadhaar number (12 digits)
  - PAN number (10 characters)
  - Driving license (optional)
- Document upload (if backend supports)
- Submit button

**Submit API:** `POST /users/kyc/submit`

### 8. **Transactions** (`/customer/transactions`)
**API:** `GET /transactions/history?page=1&page_size=10` (requires auth)

**Features:**
- Paginated list (10 per page)
- Filter by type (All/Fuel Purchase/Recharge)
- Transaction cards showing:
  - Type badge (color-coded)
  - Amount (green for credit, black for fuel purchase, red for debit)
  - Date/time
  - Transaction ID
  - Status badge
  - Fuel details (if fuel purchase)
- Pull-to-refresh
- Infinite scroll or pagination buttons

**Transaction Types:**
- `fuel_purchase` → Black text, debit
- `wallet_recharge` → Green text, credit
- `refund` → Green text, credit

---

## ⛽ Pump Operator App Features

### 1. **Authentication**
- Same flow as customer but role = "pump_owner"
- After login → Pump setup/selection

### 2. **Dashboard** (`/pump/dashboard`)
**Display:**
- Today's sales summary
- Total transactions count
- Revenue (₹X,XXX)
- Recent transactions (last 5)
- Quick actions: Scanner, Settings

### 3. **Settings** (`/pump/settings`)
**API:** 
- `GET /settings/{pump_id}` (requires pump auth)
- `POST /settings/save` (requires pump auth)

**Features:**
- Pump ID display
- Pump name input
- Petrol price per liter input
- Diesel price per liter input (if supported)
- Save button
- Auto-load on mount

**Request Format:**
```json
{
  "pump_id": 1,
  "pump_name": "ABC Petrol Pump",
  "petrol_price": "105.50"
}
```

### 4. **QR Scanner** (`/pump/scanner`)
**Features:**
- Camera viewfinder (full screen)
- Scan button / Auto-scan
- Manual QR input option
- Customer info display (after scan):
  - Name
  - Phone number
  - Wallet balance
  - Vehicle info

**Validation API:** `POST http://YOUR_SERVER:8001/qr/validate`
```json
{
  "qr_data": "encrypted_qr_string"
}
```

**Response:**
```json
{
  "valid": true,
  "user_id": 7,
  "user_name": "John Doe",
  "user_phone": "9131796052",
  "wallet_balance": 1000.00,
  "vehicle_number": "MH12AB1234",
  "vehicle_type": "Car"
}
```

**Purchase Form:**
- Fuel type selector (Petrol/Diesel)
- Quantity input (liters)
- Rate per liter (auto-filled from settings)
- Total amount (calculated)
- "Complete Purchase" button

**Purchase API:** `POST http://YOUR_SERVER:8001/transactions/fuel-purchase`
```json
{
  "qr_code": "encrypted_qr_string",
  "pump_id": 1,
  "fuel_type": "petrol",
  "fuel_quantity": 10.5,
  "fuel_rate": 105.50
}
```

**Success Response:**
```json
{
  "success": true,
  "message": "Fuel purchase successful",
  "data": {
    "transaction_id": "ZP1234567890",
    "amount": 1107.75,
    "balance_remaining": 892.25
  }
}
```

### 5. **Transactions** (`/pump/transactions`)
**API:** `GET http://YOUR_SERVER:8000/api/v1/transactions/pump/{pump_id}/history?page=1&page_size=10`

**Note:** This endpoint is on the **Customer API** (port 8000) but requires **Pump authentication token**.

**Features:**
- List all fuel purchases at this pump
- Pagination
- Filter by date range
- Transaction details:
  - Customer name
  - Amount
  - Fuel type & quantity
  - Date/time
  - Transaction ID

### 6. **Operators** (`/pump/operators`)
**Features:**
- List of pump operators
- Add operator button
- Remove/deactivate operator
- Operator permissions (if implemented)

---

## 🔧 Technical Specifications

### State Management
- Use React Context or Redux for:
  - Authentication state (tokens, user info)
  - Current role (customer/pump)
  - Wallet balance (cache)
  - App settings

### API Integration
- Use Axios or Fetch with interceptors:
  - Auto-add `Authorization: Bearer {token}` header
  - Handle 401 → Refresh token → Retry request
  - Global error handling

**Example Axios Setup:**
```javascript
const api = axios.create({
  baseURL: 'http://YOUR_SERVER_IP:8000/api/v1',
});

api.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Refresh token logic
      const newToken = await refreshToken();
      // Retry original request
    }
    return Promise.reject(error);
  }
);
```

### Storage
- **Secure Storage** for:
  - JWT tokens (access + refresh)
  - User ID
  - Role selection
- **Regular Storage** for:
  - App preferences
  - Cache (with expiration)

### Error Handling
- Network errors → "Check your internet connection"
- 401 Unauthorized → Redirect to login
- 403 Forbidden → "You don't have permission"
- 404 Not Found → "Resource not found"
- 500 Server Error → "Something went wrong. Please try again."
- Validation errors → Show field-specific messages

### Loading States
- Show skeleton loaders or spinners
- Disable buttons during API calls
- Show progress indicators for long operations

---

## 📱 Screen Flow Diagrams

### Customer Flow
```
Role Selection
    ↓
[Customer Selected]
    ↓
Login/Sign Up
    ↓
Dashboard
    ├─→ Wallet → Recharge → Payment Gateway
    ├─→ QR Generate → Display QR → Share/Download
    ├─→ Profile → Edit → Save
    ├─→ KYC → Submit Documents
    └─→ Transactions → View Details
```

### Pump Operator Flow
```
Role Selection
    ↓
[Pump Operator Selected]
    ↓
Login/Sign Up
    ↓
Dashboard
    ├─→ Settings → Configure Pump → Save
    ├─→ Scanner → Scan QR → Validate → Enter Purchase → Complete
    ├─→ Transactions → View History
    └─→ Operators → Manage Team
```

---

## 🎯 Key Features to Implement

### Must-Have
1. ✅ Role-based authentication (Customer/Pump)
2. ✅ QR code generation & scanning
3. ✅ Wallet balance display & recharge
4. ✅ Fuel purchase processing
5. ✅ Transaction history (both sides)
6. ✅ Profile management
7. ✅ KYC submission
8. ✅ Real-time balance updates
9. ✅ SMS notifications (backend handles)

### Nice-to-Have
1. 📊 Analytics dashboard (sales charts)
2. 📱 Push notifications
3. 🔔 Transaction alerts
4. 💳 Multiple payment methods
5. 🎫 Loyalty points/rewards
6. 📍 Nearby pumps finder
7. ⏰ Fuel price alerts
8. 📄 Receipt generation (PDF)
9. 🔍 Advanced transaction filters
10. 👥 Multi-pump operator support

---

## 🚨 Important Notes

### Backend Connection
- **Two separate APIs**: Customer API (port 8000) and Pump API (port 8001)
- **CORS enabled**: Backend accepts requests from any origin
- **Authentication**: JWT Bearer tokens required for protected endpoints
- **Token Storage**: Store tokens securely (use secure storage, not plain localStorage)

### QR Code Handling
- QR codes are **encrypted** and **time-limited** (default 30 minutes)
- QR validation happens on **Pump API** (port 8001) - no auth required
- Fuel purchase happens on **Pump API** (port 8001) - requires pump auth
- QR codes contain: `user_id`, `qr_id`, `type`, `created_at`

### Transaction Flow
1. Customer generates QR → Shows to pump operator
2. Pump operator scans QR → Validates (no auth needed)
3. Pump operator enters purchase details → Completes purchase (pump auth required)
4. Backend processes:
   - Validates QR code
   - Checks wallet balance
   - Deducts amount
   - Creates transaction record
   - Sends SMS to customer
   - Returns success response

### Error Scenarios
- **QR Expired**: Show "QR code has expired. Please generate a new one."
- **Insufficient Balance**: Show "Insufficient wallet balance. Please recharge."
- **Invalid QR**: Show "Invalid QR code. Please scan again."
- **Network Error**: Show "Connection failed. Check your internet."

---

## 📋 API Response Formats

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### Error Response
```json
{
  "detail": "Error message here"
}
```

### Paginated Response
```json
{
  "transactions": [...],
  "total_count": 100,
  "page": 1,
  "page_size": 10,
  "total_pages": 10
}
```

---

## 🎨 Component Library Suggestions

### UI Components Needed
1. **Buttons**: Primary, Secondary, Outline, Text
2. **Input Fields**: Text, Number, Phone, Password, OTP (6-digit)
3. **Cards**: Elevated, Outlined, Filled
4. **Badges**: Status, Type, Count
5. **Modals**: Confirmation, Info, Error
6. **Loading**: Spinner, Skeleton, Progress bar
7. **Navigation**: Bottom tabs, Top nav, Drawer
8. **Lists**: Transaction list, Settings list
9. **Forms**: Multi-step, Validation
10. **QR**: Scanner, Generator, Display

### Icons Needed
- Wallet, QR Code, Fuel Pump, Transaction, Profile, Settings
- Success, Error, Warning, Info
- Navigation icons (Home, History, Profile, etc.)

---

## 🔒 Security Considerations

1. **Token Storage**: Use secure storage (Keychain/Keystore), never plain text
2. **HTTPS**: Use HTTPS in production (backend should support)
3. **Input Validation**: Validate all user inputs client-side
4. **Error Messages**: Don't expose sensitive info in errors
5. **Biometric Auth**: Optional but recommended for login
6. **Session Timeout**: Auto-logout after inactivity (30 minutes)

---

## 📱 Platform-Specific Notes

### React Native / Flutter
- Use camera library for QR scanning (react-native-camera, flutter_barcode_scanner)
- Use secure storage (react-native-keychain, flutter_secure_storage)
- Handle deep linking for QR codes
- Background tasks for token refresh

### Web (PWA)
- Use WebRTC for camera access
- Service workers for offline support
- Install prompt for "Add to Home Screen"
- Responsive design for mobile/tablet/desktop

---

## 🧪 Testing Checklist

- [ ] Login/Signup flow
- [ ] QR generation & display
- [ ] QR scanning & validation
- [ ] Fuel purchase flow
- [ ] Wallet recharge
- [ ] Transaction history
- [ ] Profile update
- [ ] KYC submission
- [ ] Token refresh
- [ ] Error handling
- [ ] Offline handling
- [ ] Network error recovery

---

## 📞 Support & Documentation

**Backend API Documentation**: Available at `http://YOUR_SERVER:8000/docs` (Swagger UI)

**Health Check**: `GET http://YOUR_SERVER:8000/health`

**Backend Repo Structure**:
```
backend/
├── app/
│   ├── routers/        # API endpoints
│   ├── models.py       # Database models
│   ├── schemas.py      # Request/Response schemas
│   ├── services/       # Business logic
│   └── database.py     # DB connection
└── frontend-react/      # Current React web app (reference)
```

---

## 🎯 Final Checklist for Firebase Studio

When building the app, ensure:

1. ✅ **Two separate app flows** (Customer & Pump Operator)
2. ✅ **Proper authentication** with JWT token management
3. ✅ **QR code scanning** works on real devices
4. ✅ **API integration** with error handling
5. ✅ **Beautiful, modern UI** matching the design specs
6. ✅ **Responsive design** for all screen sizes
7. ✅ **Loading & error states** for all async operations
8. ✅ **Secure token storage**
9. ✅ **Real-time updates** where applicable
10. ✅ **Smooth animations** and transitions

---

**Good luck building ZapPay! 🚀⛽💰**

