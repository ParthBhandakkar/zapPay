import asyncio
import logging
import smtplib
from email.mime.text import MIMEText

import httpx

from app.config import settings
from app.database import get_redis_client
from app.realtime import connection_manager

logger = logging.getLogger("zappay.services.notifications")


# ── SMS ────────────────────────────────────────────────────────────────
async def send_sms_fast2sms(phone_number: str, message: str) -> bool:
    if not settings.sms_api_key:
        logger.warning("SMS_API_KEY not set — skipping SMS to %s", phone_number)
        return False
    url = "https://www.fast2sms.com/dev/bulkV2"
    headers = {"authorization": settings.sms_api_key, "Content-Type": "application/json"}
    payload = {
        "sender_id": settings.sms_sender_id,
        "message": message,
        "language": "english",
        "route": "q",
        "numbers": phone_number,
    }
    try:
        async with httpx.AsyncClient(timeout=10) as client:
            resp = await client.post(url, json=payload, headers=headers)
            resp.raise_for_status()
            logger.info("SMS sent to %s via Fast2SMS", phone_number)
            return True
    except Exception as e:
        logger.error("Fast2SMS failed for %s: %s", phone_number, e)
        return False


async def send_sms_twilio(phone_number: str, message: str) -> bool:
    if not settings.twilio_account_sid:
        logger.warning("Twilio not configured — skipping SMS")
        return False
    url = f"https://api.twilio.com/2010-04-01/Accounts/{settings.twilio_account_sid}/Messages.json"
    payload = {"To": phone_number, "From": settings.twilio_phone_number, "Body": message}
    try:
        async with httpx.AsyncClient(timeout=10) as client:
            resp = await client.post(url, data=payload, auth=(settings.twilio_account_sid, settings.twilio_auth_token))
            resp.raise_for_status()
            logger.info("SMS sent to %s via Twilio", phone_number)
            return True
    except Exception as e:
        logger.error("Twilio failed for %s: %s", phone_number, e)
        return False


async def send_sms(phone_number: str, message: str) -> bool:
    if settings.sms_provider == "twilio":
        return await send_sms_twilio(phone_number, message)
    return await send_sms_fast2sms(phone_number, message)


async def send_otp_sms(phone_number: str, otp_code: str) -> bool:
    message = f"Your ZapPay OTP is: {otp_code}. Valid for 10 minutes."
    return await send_sms(phone_number, message)


# ── Email ──────────────────────────────────────────────────────────────
def _send_smtp_sync(msg: MIMEText, to_email: str):
    if not settings.smtp_username or not settings.smtp_password:
        logger.warning("SMTP not configured — skipping email to %s", to_email)
        return False
    try:
        with smtplib.SMTP(settings.smtp_host, settings.smtp_port) as server:
            if settings.smtp_tls:
                server.starttls()
            server.login(settings.smtp_username, settings.smtp_password)
            server.sendmail(settings.email_from, [to_email], msg.as_string())
        logger.info("Email sent to %s", to_email)
        return True
    except Exception as e:
        logger.error("SMTP failed for %s: %s", to_email, e)
        return False


async def send_email(to_email: str, subject: str, body: str) -> bool:
    msg = MIMEText(body, "html")
    msg["Subject"] = subject
    msg["From"] = settings.email_from
    msg["To"] = to_email
    loop = asyncio.get_event_loop()
    return await loop.run_in_executor(None, _send_smtp_sync, msg, to_email)


# ── WebSocket Push ─────────────────────────────────────────────────────
async def push_user_event(user_id: int, event: str, data: dict):
    try:
        await connection_manager.send_to_user(user_id, {"event": event, "data": data})
    except Exception as e:
        logger.warning("WebSocket push failed for user %d: %s", user_id, e)


# ── High-level Notifications ───────────────────────────────────────────
async def notify_wallet_recharge(user_id: int, phone: str, email: str | None, amount: float, balance: float, transaction_id: str):
    message = f"ZapPay: Your wallet has been recharged with ₹{amount:.2f}. New balance: ₹{balance:.2f}. Txn: {transaction_id}"
    asyncio.create_task(send_sms(phone, message))
    if email:
        asyncio.create_task(send_email(email, "Wallet Recharged", f"<p>{message}</p>"))
    asyncio.create_task(push_user_event(user_id, "wallet_recharged", {
        "amount": amount,
        "balance": balance,
        "transaction_id": transaction_id,
    }))


async def notify_fuel_purchase(user_id: int, phone: str, email: str | None, amount: float, balance: float, transaction_id: str, pump_name: str, fuel_type: str, fuel_qty: float):
    message = (
        f"ZapPay: Fuel purchase of ₹{amount:.2f} at {pump_name}. "
        f"{fuel_type}: {fuel_qty:.1f}L. New balance: ₹{balance:.2f}. Txn: {transaction_id}"
    )
    asyncio.create_task(send_sms(phone, message))
    if email:
        asyncio.create_task(send_email(email, "Fuel Purchase Confirmed", f"<p>{message}</p>"))
    asyncio.create_task(push_user_event(user_id, "fuel_purchase", {
        "amount": amount,
        "balance": balance,
        "transaction_id": transaction_id,
        "pump_name": pump_name,
        "fuel_type": fuel_type,
        "fuel_qty": fuel_qty,
    }))
