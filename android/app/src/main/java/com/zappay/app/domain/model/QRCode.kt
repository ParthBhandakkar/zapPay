package com.zappay.app.domain.model

data class QRCode(
    val id: Int,
    val qrString: String,
    val isActive: Boolean,
    val expiresAt: String?,
)
