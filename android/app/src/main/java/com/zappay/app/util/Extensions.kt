package com.zappay.app.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Double.formatCurrency(): String {
    return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(this)
}

fun String.formatDate(): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(this) ?: return this
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        formatter.format(date)
    } catch (_: Exception) { this }
}

fun String.normalizePhone(): String {
    return replace(" ", "").replace("-", "").replace("+", "")
}
