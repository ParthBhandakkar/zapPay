# =============================================================================
# Stage 1: Build / Install dependencies
# =============================================================================
FROM python:3.11-slim AS builder

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

WORKDIR /build

COPY requirements.txt .
RUN pip install --user --no-warn-script-location -r requirements.txt


# =============================================================================
# Stage 2: Runtime
# =============================================================================
FROM python:3.11-slim AS runtime

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PATH="/root/.local/bin:$PATH"

# System deps for psycopg2, Pillow, EasyOCR
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libpq5 \
        libglib2.0-0 \
        libsm6 \
        libxext6 \
        libxrender-dev \
        libgomp1 \
        curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy installed packages from builder
COPY --from=builder /root/.local /root/.local

# Application code
COPY . .

# Make entrypoint executable (before switching user)
RUN chmod +x entrypoint.sh

# Non-root user
RUN groupadd -r zappay && useradd -r -g zappay -d /app -s /sbin/nologin zappay && \
    chown -R zappay:zappay /app/uploads 2>/dev/null || true

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=10s --start-period=15s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8000}/health || exit 1

USER zappay

CMD ["./entrypoint.sh"]
