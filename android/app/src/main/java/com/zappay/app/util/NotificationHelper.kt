package com.zappay.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "pump_purchases"
    private const val CHANNEL_NAME = "Fuel Purchases"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for fuel purchase transactions"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showPurchaseNotification(
        context: Context,
        notificationId: Int,
        customerName: String?,
        fuelType: String?,
        quantity: Double?,
        total: String?,
    ) {
        val title = "Payment Successful ✅"
        val body = buildString {
            append("₹$total ")
            append(fuelType ?: "Fuel")
            append(" · ${"%.2f".format(quantity ?: 0.0)}L")
            if (customerName != null) append(" · $customerName")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission not granted on Android 13+
        }
    }
}
