# 🔌 ZapPay API Endpoints Reference

## Base URLs
- **Customer API**: `http://localhost:8000/api/v1`
- **Pump API**: `http://localhost:8001`

---

## 🔐 Authentication Endpoints

### Register User
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
```

### Verify OTP
```
POST /auth/verify-otp
Body: { phone_number: string, otp: string }
Response: { success: true, message: "OTP verified" }
```

### Login
```
POST /auth/login
Body: { phone_number: string, password: string }
Response: {
  access_token: string,
  refresh_token: string,
  token_type: "bearer",
  expires_in: 1800
}
```

### Refresh Token
```
POST /auth/refresh
Body: { refresh_token: string }
Response: { access_token: string, refresh_token: string }
```

---

## 💰 Wallet Endpoints (Customer API)

### Get Balance
```
GET /wallet/balance
Headers: Authorization: Bearer {token}
Response: {
  id: number,
  user_id: number,
  balance: number,
  total_recharged: number,
  total_spent: number
}
```

### Get Wallet Summary
```
GET /wallet/summary
Headers: Authorization: Bearer {token}
Response: {
  balance: number,
  total_recharged: number,
  total_spent: number,
  recent_transactions: [...]
}
```

### Recharge Wallet
```
POST /wallet/recharge
Headers: Authorization: Bearer {token}
Body: {
  amount: number,
  payment_method: "razorpay" | "stripe"
}
Response: {
  success: true,
  payment_url: string,
  order_id: string
}
```

---

## 📱 QR Code Endpoints (Customer API)

### Generate QR Code
```
POST /qr/generate
Headers: Authorization: Bearer {token}
Body: {
  qr_type: "mobile" | "sticker",
  expires_in_minutes: number (default: 30)
}
Response: {
  success: true,
  data: {
    qr_code_id: number,
    qr_code: string (encrypted),
    qr_image_url: string,
    expires_at: string (ISO datetime)
  }
}
```

### Get My QR Codes
```
GET /qr/my-codes?active_only=true
Headers: Authorization: Bearer {token}
Response: [
  {
    id: number,
    qr_type: string,
    is_active: boolean,
    expires_at: string,
    created_at: string
  }
]
```

### Get QR Image
```
GET /qr/{qr_id}/image
Headers: Authorization: Bearer {token}
Response: Image file (PNG)
```

### Deactivate QR Code
```
POST /qr/{qr_id}/deactivate
Headers: Authorization: Bearer {token}
Response: { success: true, message: "QR code deactivated" }
```

---

## 👤 User Profile Endpoints (Customer API)

### Get Profile
```
GET /users/profile
Headers: Authorization: Bearer {token}
Response: {
  id: number,
  phone_number: string,
  email: string,
  full_name: string,
  role: string,
  kyc_status: "pending" | "verified" | "rejected",
  vehicle_number: string,
  vehicle_type: string,
  address: string,
  city: string,
  state: string,
  pincode: string,
  created_at: string
}
```

### Update Profile
```
PUT /users/profile
Headers: Authorization: Bearer {token}
Body: {
  full_name?: string,
  email?: string,
  address?: string,
  city?: string,
  state?: string,
  pincode?: string,
  vehicle_number?: string,
  vehicle_type?: string
}
Response: Updated user object
```

### Get KYC Status
```
GET /users/kyc/status
Headers: Authorization: Bearer {token}
Response: {
  kyc_status: "pending" | "verified" | "rejected",
  aadhaar_number: string (masked),
  pan_number: string (masked),
  driving_license: string (masked)
}
```

### Submit KYC
```
POST /users/kyc/submit
Headers: Authorization: Bearer {token}
Body: {
  aadhaar_number: string (12 digits),
  pan_number: string (10 characters),
  driving_license?: string
}
Response: { success: true, message: "KYC submitted" }
```

---

## 💳 Transaction Endpoints

### Get User Transaction History (Customer API)
```
GET /transactions/history?page=1&page_size=10&transaction_type=fuel_purchase
Headers: Authorization: Bearer {token}
Response: {
  transactions: [
    {
      id: number,
      transaction_id: string,
      transaction_type: "fuel_purchase" | "wallet_recharge" | "refund",
      amount: number,
      status: "completed" | "pending" | "failed",
      fuel_type: string,
      fuel_quantity: number,
      fuel_rate: number,
      pump_id: number,
      pump_name: string,
      created_at: string,
      completed_at: string
    }
  ],
  total_count: number,
  page: number,
  page_size: number,
  total_pages: number
}
```

### Get Pump Transaction History (Customer API - requires Pump token)
```
GET /transactions/pump/{pump_id}/history?page=1&page_size=10
Headers: Authorization: Bearer {pump_token}
Response: Same as above
```

### Get Transaction Details
```
GET /transactions/{transaction_id}
Headers: Authorization: Bearer {token}
Response: Full transaction object
```

---

## ⛽ Pump Operator Endpoints (Pump API - Port 8001)

### Validate QR Code
```
POST /qr/validate
Body: {
  qr_data: string (encrypted QR string)
}
Response: {
  valid: true,
  user_id: number,
  user_name: string,
  user_phone: string,
  qr_code_id: number,
  qr_type: string,
  wallet_balance: number,
  vehicle_number: string,
  vehicle_type: string
}
```

### Process Fuel Purchase
```
POST /transactions/fuel-purchase
Headers: Authorization: Bearer {pump_token}
Body: {
  qr_code: string (encrypted QR string),
  pump_id: number,
  fuel_type: "petrol" | "diesel",
  fuel_quantity: number (liters),
  fuel_rate: number (price per liter)
}
Response: {
  success: true,
  message: "Fuel purchase successful",
  data: {
    transaction_id: string,
    amount: number,
    balance_remaining: number
  }
}
```

### Get Pump Settings
```
GET /settings/{pump_id}
Headers: Authorization: Bearer {pump_token}
Response: {
  pump_id: number,
  pump_name: string,
  petrol_price: string
}
```

### Save Pump Settings
```
POST /settings/save
Headers: Authorization: Bearer {pump_token}
Body: {
  pump_id: number,
  pump_name: string,
  petrol_price: string
}
Response: {
  success: true,
  message: "Settings saved",
  data: {
    pump_id: number,
    pump_name: string,
    petrol_price: string
  }
}
```

### Get Pump Details
```
GET /pumps/{pump_id}
Headers: Authorization: Bearer {pump_token}
Response: {
  id: number,
  name: string,
  address: string,
  city: string,
  state: string,
  pincode: string,
  owner_id: number
}
```

### Get Operator Profile
```
GET /operators/profile
Headers: Authorization: Bearer {pump_token}
Response: User profile object
```

---

## 🔔 WebSocket Endpoints (Customer API)

### Real-time Updates
```
WS /ws/{user_id}
Headers: Authorization: Bearer {token}
Purpose: Receive real-time transaction notifications
```

---

## 📊 Admin Endpoints (Customer API)

### Admin Dashboard
```
GET /admin/dashboard
Headers: Authorization: Bearer {admin_token}
Response: {
  total_users: number,
  total_pumps: number,
  total_transactions: number,
  total_revenue: number,
  recent_activity: [...]
}
```

---

## ⚠️ Error Responses

All endpoints may return these error formats:

### 400 Bad Request
```json
{
  "detail": "Validation error message"
}
```

### 401 Unauthorized
```json
{
  "detail": "Not authenticated"
}
```

### 403 Forbidden
```json
{
  "detail": "Not authorized to access this resource"
}
```

### 404 Not Found
```json
{
  "detail": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "detail": "Internal server error"
}
```

---

## 🔑 Authentication Headers

All protected endpoints require:
```
Authorization: Bearer {access_token}
```

Tokens are obtained from `/auth/login` endpoint.

---

## 📝 Notes

1. **Two APIs**: Customer API (8000) and Pump API (8001)
2. **QR Validation**: No auth required (public endpoint)
3. **Fuel Purchase**: Requires pump operator authentication
4. **Pump Transactions**: Uses Customer API but requires pump token
5. **CORS**: Enabled for all origins
6. **Content-Type**: `application/json` for all POST/PUT requests

---

## 🧪 Testing

Use Swagger UI at: `http://YOUR_SERVER:8000/docs` for interactive API testing.

