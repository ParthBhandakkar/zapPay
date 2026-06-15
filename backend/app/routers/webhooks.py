from fastapi import APIRouter, Header, HTTPException, Request, Depends, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.services.payment import credit_wallet
from app.models import TransactionType, User
from app.config import settings
import hmac
import hashlib
import json
import stripe

router = APIRouter()

@router.post("/razorpay")
async def razorpay_webhook(request: Request, x_razorpay_signature: str = Header(None), db: Session = Depends(get_db)):
	body = await request.body()
	if not settings.razorpay_key_secret:
		raise HTTPException(status_code=400, detail="Razorpay not configured")
	# Verify signature
	expected = hmac.new(bytes(settings.razorpay_key_secret, 'utf-8'), body, hashlib.sha256).hexdigest()
	if expected != x_razorpay_signature:
		raise HTTPException(status_code=400, detail="Invalid signature")
	payload = json.loads(body.decode())
	# Minimal handling: capture payment.succeeded for wallet topups
	if payload.get('event') == 'payment.captured':
		notes = payload.get('payload', {}).get('payment', {}).get('entity', {}).get('notes', {})
		user_id = notes.get('user_id')
		amount = payload.get('payload', {}).get('payment', {}).get('entity', {}).get('amount')
		if user_id and amount:
			amount_rupees = float(amount) / 100.0
			user = db.query(User).filter(User.id == int(user_id)).first()
			if user:
				transaction = credit_wallet(db, user.id, amount_rupees, TransactionType.WALLET_RECHARGE)
				try:
					import asyncio
					from app.services.notifications import notify_wallet_recharge
					asyncio.create_task(
						notify_wallet_recharge(user.id, user.phone_number, user.email, amount_rupees, transaction.wallet.balance, transaction.transaction_id)
					)
				except Exception:
					pass
	return {"status": "ok"}

@router.post("/stripe")
async def stripe_webhook(request: Request, stripe_signature: str = Header(None), db: Session = Depends(get_db)):
	if not settings.stripe_webhook_secret:
		raise HTTPException(status_code=400, detail="Stripe not configured")
	payload = await request.body()
	try:
		event = stripe.Webhook.construct_event(payload, stripe_signature, settings.stripe_webhook_secret)
	except Exception as e:
		raise HTTPException(status_code=400, detail="Invalid signature")
	# Handle successful payment intent
	if event['type'] == 'payment_intent.succeeded':
		intent = event['data']['object']
		metadata = intent.get('metadata', {}) or {}
		user_id = metadata.get('user_id')
		amount = intent.get('amount_received') or intent.get('amount')
		if user_id and amount:
			amount_rupees = float(amount) / 100.0
			user = db.query(User).filter(User.id == int(user_id)).first()
			if user:
				transaction = credit_wallet(db, user.id, amount_rupees, TransactionType.WALLET_RECHARGE)
				try:
					import asyncio
					from app.services.notifications import notify_wallet_recharge
					asyncio.create_task(
						notify_wallet_recharge(user.id, user.phone_number, user.email, amount_rupees, transaction.wallet.balance, transaction.transaction_id)
					)
				except Exception:
					pass
	return {"status": "ok"} 