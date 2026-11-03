import json
import logging

from sqlalchemy.orm import Session

from app.models import AuditEvent

logger = logging.getLogger("zappay.services.audit")


def log_audit_event(
    db: Session,
    actor_id: int = None,
    actor_role: str = None,
    action: str = None,
    resource_type: str = None,
    resource_id: str = None,
    details: dict = None,
    ip_address: str = None,
):
    if not all([action, resource_type]):
        logger.warning("Audit event skipped: action and resource_type required")
        return

    try:
        event = AuditEvent(
            actor_id=actor_id,
            actor_role=actor_role,
            action=action,
            resource_type=resource_type,
            resource_id=str(resource_id) if resource_id else None,
            details=json.dumps(details) if details else None,
            ip_address=ip_address,
        )
        db.add(event)
        db.commit()
        logger.debug("Audit event logged: %s %s %s", action, resource_type, resource_id)
    except Exception as e:
        logger.warning("Failed to log audit event: %s", e)
        db.rollback()
