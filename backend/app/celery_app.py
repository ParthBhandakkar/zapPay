import logging

from celery import Celery
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.config import settings

logger = logging.getLogger("zappay.celery")

celery_app = Celery("zappay", broker=settings.redis_url, backend=settings.redis_url)

celery_app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="UTC",
    enable_utc=True,
    task_track_started=True,
    task_time_limit=30 * 60,
    task_soft_time_limit=25 * 60,
    worker_prefetch_multiplier=1,
)


def get_db_session():
    engine = create_engine(settings.database_url_with_ssl)
    return sessionmaker(autocommit=False, autoflush=False, bind=engine)()


@celery_app.task(bind=True, max_retries=3, default_retry_delay=60)
def check_auto_recharge(self):
    db = get_db_session()
    try:
        from app.models import User, Wallet
        users = (
            db.query(User)
            .join(Wallet)
            .filter(
                User.auto_recharge_enabled == True,
                Wallet.balance <= User.auto_recharge_threshold,
            )
            .all()
        )
        logger.info("Auto-recharge check: %d users below threshold", len(users))

        for user in users:
            amount = user.auto_recharge_amount or 0
            method = user.auto_recharge_payment_method or "razorpay"
            if amount <= 0:
                continue
            try:
                from app.services.payment import create_razorpay_order, create_stripe_payment_intent
                if method == "razorpay":
                    create_razorpay_order(amount, user_id=user.id)
                    logger.info("Auto-recharge order for user %d: %.2f via Razorpay", user.id, amount)
                elif method == "stripe":
                    create_stripe_payment_intent(amount, user_id=user.id)
                    logger.info("Auto-recharge intent for user %d: %.2f via Stripe", user.id, amount)
            except Exception as e:
                logger.error("Auto-recharge failed for user %d: %s", user.id, e)
    except Exception as e:
        logger.exception("Auto-recharge check failed: %s", e)
        raise self.retry(exc=e)
    finally:
        db.close()
