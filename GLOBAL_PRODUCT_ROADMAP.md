# ZapPay Global Product Roadmap

Last reviewed: 2026-06-19

## 1. Product Vision

ZapPay should evolve from a fuel wallet and QR payment app into a global fuel and mobility payments platform.

The long-term product should serve three major groups:

- Customers who want fast, trusted fuel and energy payments, vehicle expense history, receipts, offers, and vehicle insights.
- Petrol pumps and fuel retailers who need payment acceptance, operator control, pricing, inventory, settlements, reconciliation, and analytics.
- ZapPay operations/admin teams who need onboarding, compliance controls, fraud monitoring, customer support, settlement controls, and market configuration.

The global product should support petrol, diesel, CNG, LPG, EV charging, premium grades, fleet fueling, country-specific payment methods, multiple currencies, regional taxes, and localized compliance rules.

## 2. Current Product Foundation

The current repo already includes these useful building blocks:

- Android customer app with wallet, QR, profile, vehicle data, history, and recharge paths.
- Android pump app with scanner, vehicle lookup, fuel purchase, pump dashboard, and settings.
- FastAPI backend with auth, users, wallet, QR, transactions, pumps, pump operations, notifications, and webhooks.
- Basic pump settings, fuel purchase, wallet debit/credit, transaction history, and settlement scaffolding.
- React/web frontend experiments for customer and pump interfaces.
- Production deployment notes, Render configuration, and Android build assets.

The current app is a strong MVP/prototype. To become global and production-grade, it needs stronger money movement, operational tooling, compliance, observability, localized configuration, and richer workflows.

## 3. Product Principles

- Payment safety first: never double charge, never lose ledger state, always show a receipt.
- Operator speed: a pump attendant should complete a transaction in seconds even during rush hours.
- Customer trust: every debit must have a clear reason, pump, vehicle, fuel details, and support path.
- Global by configuration: country, currency, unit, tax, language, payment rail, and compliance should be configurable, not hardcoded.
- Offline-tolerant where possible: petrol pumps often have weak network; the product should degrade gracefully.
- Audit everything: money, roles, settings, refunds, settlements, and admin actions must be traceable.
- Start narrow, build deeply: first make one country and one fuel payment flow excellent, then expand.

## 4. Customer App Requirements

### 4.1 Onboarding

- Country selector.
- Language selector.
- Currency display based on country.
- Phone login, email login, and OTP login options depending on market.
- Customer role selection.
- Vehicle setup during onboarding.
- Optional KYC flow based on country and transaction limits.
- Consent screens for privacy, marketing, location, and notifications.

### 4.2 Customer Profile

- Name, phone, email, address.
- Country, region/state, city, postal code.
- Identity/KYC status where required.
- Vehicle list rather than single vehicle:
  - Vehicle number.
  - Vehicle type.
  - Fuel type compatibility.
  - Nickname.
  - Odometer.
  - Fleet/company association.
- Saved payment methods where legally and technically allowed.
- Notification preferences.
- Privacy controls:
  - Export data.
  - Delete account.
  - Revoke consent.
  - Device/session logout.

### 4.3 Fuel Discovery

- Nearby pump list.
- Nearby pump map.
- Search by location.
- Filters:
  - Fuel type.
  - Open now.
  - Price.
  - Verified pump.
  - Amenities.
  - EV charging.
- Pump detail page:
  - Name, address, distance.
  - Fuel prices.
  - Available fuel grades.
  - Open/closed status.
  - Amenities.
  - Ratings/support status later.

### 4.4 Payments

- QR pay.
- Vehicle-number pay.
- Wallet pay.
- Card pay through compliant gateways.
- Local payment rails:
  - India: UPI, cards, wallets, net banking.
  - EU/UK: cards, SEPA where relevant.
  - US: cards, ACH for fleet.
  - Other countries: local wallets and bank transfer rails.
- Auto-top-up.
- Saved wallet balance.
- Transaction limits by market, risk, and KYC status.
- Payment confirmation before debit:
  - Pump.
  - Fuel type.
  - Quantity.
  - Rate.
  - Total.
  - Vehicle.
- Success screen with receipt.
- Failed payment recovery.
- Refund/dispute request.

### 4.5 Transaction History and Receipts

- Transaction list with filters:
  - Fuel purchases.
  - Wallet recharges.
  - Refunds.
  - Failed transactions.
  - Vehicle.
  - Date range.
  - Pump.
- Transaction detail page:
  - Receipt number.
  - Transaction ID.
  - Pump details.
  - Customer details.
  - Vehicle details.
  - Fuel type, quantity, rate, amount.
  - Taxes and fees.
  - Wallet opening and closing balance.
  - Payment method.
  - Status timeline.
- PDF receipt download.
- Email receipt.
- Monthly statement.
- CSV export.

### 4.6 Vehicle Insights

- Spend by vehicle.
- Fuel quantity by month.
- Average fuel price.
- Mileage/fuel efficiency if odometer is captured.
- Alerts for unusual fuel usage.
- Business reimbursement reports.

### 4.7 Retention

- Rewards wallet.
- Pump-specific loyalty.
- Referral system.
- Promo codes.
- Cashback campaigns.
- Fuel price alerts.
- Favorite pumps.

## 5. Pump App Requirements

### 5.1 Pump Onboarding

- Pump owner registration.
- Pump license upload.
- Tax registration details.
- Bank account verification.
- Location verification.
- Fuel types and grades setup.
- Default rates.
- Commission plan.
- Business hours.
- Pump photos and amenities.
- Multi-branch support for chains.

### 5.2 Operator Management

- Roles:
  - Owner.
  - Manager.
  - Cashier.
  - Nozzle attendant.
  - Auditor.
- Operator invite flow.
- Operator activation/deactivation.
- Shift assignment.
- Shift start/end.
- Device binding.
- PIN or biometric approval for sensitive actions.
- Permission controls:
  - Process payment.
  - Override rate.
  - Refund.
  - Close shift.
  - Change settings.
  - View settlement.

### 5.3 Scanner and Payment Flow

- QR scanner.
- Manual QR entry.
- Vehicle number lookup.
- Number plate OCR later.
- Fuel type selection from pump settings.
- Quantity-first mode.
- Amount-first mode.
- Saved rate auto-fill.
- Manager-authorized manual override.
- Balance check before payment.
- Clear error recovery.
- Idempotent purchase processing.
- Payment success receipt.
- Customer notification.
- Pump copy of receipt.
- Retry state for weak network.

### 5.4 Pump Dashboard

- Today revenue.
- Today transaction count.
- Fuel sold by type.
- Revenue by fuel type.
- Operator-wise sales.
- Pending settlement.
- Last settlement.
- Failed payments.
- Refunds/disputes.
- Open/closed status.
- Active rates.
- Recent transactions.
- Shift status.

### 5.5 Pricing and Inventory

- Fuel grade setup:
  - Petrol.
  - Diesel.
  - CNG.
  - LPG.
  - Premium petrol.
  - EV charging.
  - Country-specific names.
- Rate management.
- Scheduled rate changes.
- Rate change audit log.
- Inventory/tank level tracking.
- Opening stock.
- Closing stock.
- Fuel delivery entries.
- Low-stock alerts.
- Reconciliation between sold quantity and tank stock.

### 5.6 Settlements and Reconciliation

- Settlement dashboard.
- Daily settlement report.
- Settlement status:
  - Pending.
  - Processing.
  - Paid.
  - Failed.
  - Held.
- Gross sales.
- Commission.
- Taxes.
- Refunds.
- Net payout.
- Bank payout reference.
- Downloadable settlement PDF/CSV.
- Transaction-level reconciliation.
- Dispute settlement hold.

## 6. Admin Platform Requirements

### 6.1 Operations Dashboard

- Total users.
- Total pumps.
- Active markets.
- Transaction volume.
- Gross merchandise value.
- Revenue/commission.
- Failed payments.
- Refunds.
- Settlement pending amount.
- Risk alerts.

### 6.2 Pump Administration

- Pump approval queue.
- Document review.
- Pump verification.
- Pump status controls.
- Commission plan assignment.
- Bank account review.
- Pump branch management.
- Operator management.

### 6.3 Customer Administration

- User search.
- Profile view.
- Wallet view.
- Transaction history.
- KYC review.
- Account status.
- Session/device controls.
- Support notes.

### 6.4 Transaction and Risk Operations

- Global transaction search.
- Transaction detail.
- Manual refund.
- Dispute handling.
- Risk scoring.
- Fraud rules:
  - Velocity by user.
  - Velocity by pump.
  - Repeated failed QR scans.
  - Large purchases.
  - Device switching.
  - Unusual location.
- Blacklist/allowlist.
- Audit trail.

### 6.5 Configuration

- Countries.
- Currencies.
- Fuel units.
- Fuel product names.
- Taxes.
- Payment providers.
- KYC requirements.
- Transaction limits.
- Notification templates.
- Languages.
- Feature flags.

## 7. Backend and Platform Requirements

### 7.1 Money Ledger

The platform must move from simple balance mutation to a double-entry ledger.

Required account types:

- Customer wallet.
- Pump receivable.
- ZapPay commission.
- Payment gateway clearing.
- Refund clearing.
- Settlement payable.
- Tax payable where applicable.

Required ledger event types:

- Wallet recharge.
- Fuel purchase debit.
- Pump receivable credit.
- Commission credit.
- Refund reversal.
- Settlement payout.
- Adjustment.

Ledger rules:

- Every money movement must balance.
- Balance should be derived from ledger or reconciled against wallet snapshots.
- No direct wallet mutation without ledger entries.
- Every transaction must have an idempotency key.
- Every refund must reference the original transaction.

### 7.2 Payment State Machine

Transaction states should include:

- Created.
- Pending authorization.
- Authorized.
- Captured.
- Completed.
- Failed.
- Reversed.
- Refunded.
- Partially refunded.
- Settlement pending.
- Settled.

Each state transition should record:

- Actor.
- Timestamp.
- Previous state.
- Next state.
- Reason.
- Metadata.

### 7.3 Idempotency

Every payment endpoint should accept an idempotency key.

Rules:

- Same key and same payload returns original result.
- Same key and different payload is rejected.
- Keys expire after a configured time window.
- Idempotency records store request hash, status, response, and transaction reference.

### 7.4 Global Configuration

Add platform-level configuration tables:

- Country.
- Currency.
- Region.
- Fuel unit.
- Fuel product.
- Payment provider.
- Tax profile.
- Compliance profile.
- Feature flag.

These remove hardcoding of INR, rupees, litres, Indian KYC, and India-only payment rails.

### 7.5 API Standards

- Maintain a formal OpenAPI specification.
- Version APIs.
- Use consistent response envelopes.
- Use structured error codes.
- Add pagination standards.
- Add idempotency headers.
- Add webhook signatures.
- Add request tracing IDs.

The OpenAPI Specification defines a standard interface for HTTP APIs so humans and systems can understand service capabilities without reading source code.

Reference: https://swagger.io/specification/

### 7.6 Security

Mobile and backend security should align with OWASP MASVS and common API security practices.

Required controls:

- Secure token storage.
- Access token refresh.
- Device/session management.
- Role-based access control.
- Audit logs.
- Rate limiting.
- Input validation.
- Secrets management.
- Encryption in transit.
- Sensitive data encryption at rest.
- Root/jailbreak risk signals.
- Certificate pinning later.
- Logging without secrets or PII leaks.

OWASP MASVS is a mobile security verification standard for secure mobile app design, development, and testing.

Reference: https://mas.owasp.org/MASVS/

### 7.7 Compliance and Privacy

Payment and privacy requirements vary by country. The app should be compliance-ready, not compliance-hardcoded.

Required privacy features:

- Consent tracking.
- Data export.
- Account deletion.
- Data retention rules.
- Purpose-based data collection.
- Data transfer controls.
- PII access logging.

The European Commission describes GDPR/data protection as covering rules for personal data protection inside and outside the EU.

Reference: https://commission.europa.eu/law/law-topic/data-protection_en

Payment card handling should avoid storing card data directly. Use compliant payment providers and design the payment environment with PCI DSS expectations in mind.

PCI SSC maintains PCI DSS resources and documents.

Reference: https://www.pcisecuritystandards.org/standards/pci-dss/

### 7.8 Observability

- Structured logs.
- Request IDs.
- Transaction IDs in logs.
- Error tracking.
- API latency dashboards.
- Payment failure dashboards.
- Settlement health dashboards.
- Background job monitoring.
- Uptime monitoring.
- Audit log search.

## 8. Data Model Additions

Priority additions:

- countries
- currencies
- fuel_products
- pump_fuel_prices
- pump_devices
- operator_shifts
- idempotency_keys
- ledger_accounts
- ledger_entries
- receipts
- disputes
- refund_requests
- settlement_batches
- audit_events
- feature_flags
- notification_events
- support_tickets
- fleet_accounts
- fleet_vehicles
- fleet_drivers
- fleet_limits

## 9. Customer Information Architecture

Main tabs:

- Home
- Pay
- Pumps
- Wallet
- History
- Vehicles
- Profile

Home should show:

- Wallet balance.
- Primary QR/pay action.
- Nearby preferred pump.
- Recent transaction.
- Monthly spend.
- Offers.

Pay should show:

- QR code.
- Vehicle number payment option.
- Selected vehicle.
- Security timer/expiry.

History should show:

- Filters.
- Transaction cards.
- Receipt details.
- Export.

Pumps should show:

- Map/list.
- Prices.
- Availability.
- Details.

## 10. Pump Information Architecture

Main sections:

- Dashboard
- Scan & Pay
- Transactions
- Shifts
- Prices
- Inventory
- Settlements
- Operators
- Settings

Dashboard should show:

- Open/closed status.
- Active prices.
- Today revenue.
- Pending settlement.
- Operator status.
- Recent transactions.

Scan & Pay should show:

- QR scan.
- Vehicle lookup.
- Fuel type.
- Quantity/amount.
- Confirmation.
- Receipt.

Transactions should show:

- Search.
- Filters.
- Transaction detail.
- Refund/dispute actions based on permission.

Settlements should show:

- Current pending settlement.
- Past settlements.
- Download.
- Bank payout reference.

## 11. Fleet Product

Fleet accounts can become a major B2B revenue stream.

Fleet features:

- Company account.
- Admin users.
- Drivers.
- Vehicles.
- Fuel limits per driver/vehicle.
- Pump restrictions.
- Daily/monthly budgets.
- Approval workflows.
- Consolidated invoice.
- Reports.
- API export.

Fleet plans:

- Free small fleet.
- Pro fleet analytics.
- Enterprise custom integration.

## 12. Global Expansion Strategy

### 12.1 Market Readiness Checklist

For every new country:

- Currency.
- Fuel units.
- Fuel product naming.
- Tax invoice rules.
- Payment providers.
- KYC/AML requirements.
- Privacy requirements.
- Refund rules.
- Settlement timing.
- Language support.
- Support process.
- Data residency review.

### 12.2 Localization

- All app text in string resources.
- Locale-specific currency formatting.
- Locale-specific date/time formatting.
- Locale-specific fuel units.
- Right-to-left language readiness later.
- Country-specific legal content.

### 12.3 Payment Provider Abstraction

Create a provider interface:

- create_order
- verify_payment
- capture_payment
- refund_payment
- handle_webhook
- get_status

Provider implementations:

- Razorpay.
- Stripe.
- Adyen later.
- Local wallet/bank rails per market.

## 13. Development Roadmap

### Phase 1: Stabilize Core Payments

Goal: make the existing India-first flow reliable and trustworthy.

Deliverables:

- Backend settings deployed.
- Idempotent fuel purchase.
- Receipt data returned after payment.
- Transaction detail receipt endpoint.
- Better Android success/receipt screen.
- Pump dashboard rates/status.
- Backend tests for QR-to-payment.
- Android build/release process documented.

### Phase 2: Production Operations

Goal: make the platform operable by a real business team.

Deliverables:

- Admin portal.
- Pump approval.
- Transaction search.
- Refund workflow.
- Settlement reports.
- Audit logs.
- Monitoring.
- Error tracking.
- Support ticket basics.

### Phase 3: Ledger and Settlement

Goal: make money movement auditable and reconcilable.

Deliverables:

- Ledger accounts.
- Ledger entries.
- Balance reconciliation.
- Settlement batch generation.
- Settlement payout status.
- Commission reporting.
- Refund reversal entries.

### Phase 4: Customer and Pump Completeness

Goal: make both app experiences feel complete.

Deliverables:

- Nearby pumps.
- Pump details.
- Fuel price list.
- Multi-vehicle customer profile.
- Receipts/PDF exports.
- Pump operator shifts.
- Pricing schedule.
- Inventory basics.
- Notifications.

### Phase 5: Fleet and B2B

Goal: add scalable business revenue.

Deliverables:

- Fleet accounts.
- Driver/vehicle management.
- Limits and approvals.
- Fleet reports.
- Monthly invoicing.
- Pump chain dashboard.

### Phase 6: Global Platform

Goal: launch outside the first market through configuration.

Deliverables:

- Countries/currencies.
- Tax profiles.
- Payment provider abstraction.
- Language localization.
- Fuel unit configuration.
- Country-specific compliance profiles.
- Multi-region deployment plan.

## 14. Near-Term Engineering Backlog

Highest priority:

1. Add idempotency to fuel purchase endpoints.
2. Add receipt fields and transaction detail response.
3. Add pump settings persistence/deployment.
4. Add tests for payment duplicate prevention.
5. Add admin transaction search.
6. Add refund request lifecycle.
7. Add settlement batch model.
8. Add audit events.
9. Add country/currency/fuel unit config.
10. Add Android transaction detail/receipt screen.

## 15. Success Metrics

Customer metrics:

- Successful payment rate.
- Payment completion time.
- Repeat purchase rate.
- Wallet recharge conversion.
- Dispute rate.
- Refund rate.

Pump metrics:

- Active pumps.
- Transactions per pump per day.
- Operator success rate.
- Settlement accuracy.
- Time to settlement.
- Failed scan rate.

Business metrics:

- Gross transaction value.
- Net revenue.
- Commission revenue.
- Customer acquisition cost.
- Pump acquisition cost.
- Retention.
- Support tickets per 1,000 transactions.

Reliability metrics:

- API uptime.
- Payment API latency.
- Error rate.
- Webhook processing delay.
- Settlement job success.
- Duplicate charge incidents.

## 16. What To Build First

The first product-grade sprint should focus on trust:

- Idempotent payments.
- Receipts.
- Pump settings reliability.
- Better transaction detail.
- Backend tests.
- Deployment of backend fixes.

These are the right first changes because they protect money movement and make both customers and pump operators confident in the product.
