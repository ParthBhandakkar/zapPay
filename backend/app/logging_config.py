import logging
import sys
from datetime import datetime, timezone

from app.config import settings


class UTCFormatter(logging.Formatter):
    converter = datetime.fromtimestamp

    def formatTime(self, record, datefmt=None):
        dt = datetime.fromtimestamp(record.created, tz=timezone.utc)
        if datefmt:
            return dt.strftime(datefmt)
        return dt.isoformat()


def setup_logging() -> None:
    level = logging.DEBUG if settings.debug else logging.INFO

    formatter = UTCFormatter(
        fmt="%(asctime)s  %(levelname)-7s  %(name)-30s  %(message)s",
        datefmt="%Y-%m-%dT%H:%M:%S.%fZ",
    )

    handler = logging.StreamHandler(sys.stdout)
    handler.setLevel(level)
    handler.setFormatter(formatter)

    root = logging.getLogger()
    root.setLevel(level)
    root.handlers.clear()
    root.addHandler(handler)

    # Quiet noisy libs
    logging.getLogger("passlib").setLevel(logging.WARNING)
    logging.getLogger("jose").setLevel(logging.WARNING)
    logging.getLogger("urllib3").setLevel(logging.WARNING)
    logging.getLogger("httpx").setLevel(logging.WARNING)
    logging.getLogger("easyocr").setLevel(logging.WARNING)
    logging.getLogger("PIL").setLevel(logging.WARNING)
    logging.getLogger("sqlalchemy.engine").setLevel(logging.WARNING if not settings.debug else logging.INFO)
    logging.getLogger("apscheduler").setLevel(logging.WARNING)

    logger = logging.getLogger("zappay")
    logger.info("Logging configured — environment=%s level=%s", settings.environment, logging.getLevelName(level))
