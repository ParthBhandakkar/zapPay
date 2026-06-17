from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas import WalletResponse, WalletRecharge, WalletSummaryResponse, BaseResponse, WebhookVerifyRequest
from app.services.auth import get_current_user
from app.services.payment import (
    get_or_create_wallet, credit_wallet, get_wallet_summary,
    create_razorpay_order, create_stripe_payment_intent, verify_razorpay_payment
)
from app.models import TransactionType

router = APIRouter()
security = HTTPBearer()

@router.get("/balance", response_model=WalletResponse)
async def get_wallet_balance(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get current wallet balance and details."""
    
    user = get_current_user(db, credentials.credentials)
    wallet = get_or_create_wallet(db, user.id)
    
    return wallet

@router.get("/summary", response_model=WalletSummaryResponse)
async def get_wallet_summary_endpoint(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get comprehensive wallet summary with recent transactions."""
    
    user = get_current_user(db, credentials.credentials)
    summary = get_wallet_summary(db, user.id)
    
    return WalletSummaryResponse(**summary)

@router.post("/recharge/create-order", response_model=BaseResponse)
async def create_recharge_order(
    recharge_data: WalletRecharge,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Create payment order for wallet recharge."""
    
    user = get_current_user(db, credentials.credentials)
    
    if recharge_data.payment_method == "razorpay":
        order = create_razorpay_order(recharge_data.amount, user_id=user.id)
        if not order:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to create Razorpay order"
            )
        
        return BaseResponse(
            success=True,
            message="Razorpay order created successfully",
            data={
                "order_id": order["id"],
                "amount": order["amount"],
                "currency": order["currency"],
                "payment_method": "razorpay"
            }
        )
    
    elif recharge_data.payment_method == "stripe":
        intent = create_stripe_payment_intent(recharge_data.amount, user_id=user.id)
        if not intent:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to create Stripe payment intent"
            )
        
        return BaseResponse(
            success=True,
            message="Stripe payment intent created successfully",
            data={
                "client_secret": intent["client_secret"],
                "amount": intent["amount"],
                "currency": intent["currency"],
                "payment_method": "stripe"
            }
        )
    
    else:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Unsupported payment method"
        )

@router.post("/recharge/verify-payment", response_model=BaseResponse)
async def verify_payment_and_recharge(
    verify_data: WebhookVerifyRequest,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Verify payment and credit wallet."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Verify payment based on payment method
    payment_method = verify_data.payment_method
    if payment_method == "razorpay":
        signature = verify_data.signature
        if not signature:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Payment signature is required for Razorpay"
            )
        
        is_valid = verify_razorpay_payment(verify_data.payment_id, verify_data.order_id, signature)
        if not is_valid:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid payment signature"
            )
        
        # Get order details to determine amount (in production, store this during order creation)
        # For now, we'll need to fetch from Razorpay or store in Redis during order creation
        # This is a simplified implementation
        try:
            from app.services.payment import razorpay_client
            if razorpay_client:
                order_details = razorpay_client.order.fetch(verify_data.order_id)
                amount = order_details["amount"] / 100  # Convert from paisa to rupees
            else:
                raise HTTPException(
                    status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    detail="Payment gateway not configured"
                )
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to verify payment amount"
            )
    
    elif payment_method == "stripe":
        # For Stripe, you would typically verify using webhooks
        # This is a simplified implementation
        try:
            import stripe
            intent = stripe.PaymentIntent.retrieve(verify_data.payment_id)
            if intent.status != "succeeded":
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Payment not completed"
                )
            amount = intent.amount / 100  # Convert from paisa to rupees
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid payment or payment not completed"
            )
    
    else:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Unsupported payment method"
        )
    
    # Credit wallet
    transaction = credit_wallet(db, user.id, amount, TransactionType.WALLET_RECHARGE)
    
    # Notify user asynchronously
    try:
        import asyncio
        from app.services.notifications import notify_wallet_recharge
        asyncio.create_task(
            notify_wallet_recharge(user.id, user.phone_number, user.email, amount, transaction.wallet.balance, transaction.transaction_id)
        )
    except Exception:
        pass
    
    return BaseResponse(
        success=True,
        message="Wallet recharged successfully",
        data={
            "transaction_id": transaction.transaction_id,
            "amount": amount,
            "new_balance": transaction.wallet.balance
        }
    )

@router.post("/test-recharge", response_model=BaseResponse)
async def test_wallet_recharge(
    amount: float,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Test endpoint for wallet recharge (development only)."""
    
    # This endpoint should be disabled in production
    from app.config import settings
    # if not settings.debug:
    #     raise HTTPException(
    #         status_code=status.HTTP_404_NOT_FOUND,
    #         detail="Not found"
    #     )
    
    user = get_current_user(db, credentials.credentials)
    
    if amount <= 0 or amount > 10000:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid amount"
        )
    
    # Credit wallet directly for testing
    transaction = credit_wallet(db, user.id, amount, TransactionType.WALLET_RECHARGE)
    
    return BaseResponse(
        success=True,
        message="Test recharge successful",
        data={
            "transaction_id": transaction.transaction_id,
            "amount": amount,
            "new_balance": transaction.wallet.balance
        }
    ) 