package com.avidlearner.rem.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.avidlearner.rem.MainActivity
import com.avidlearner.rem.R

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Quote Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily quote notifications"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showQuoteNotification(title: String, quoteText: String, isPersistent: Boolean, color: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Convert the compose color Long to an Android color Int
        val colorInt = color.toInt()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Using default launcher icon for now
            .setContentTitle(title)
            .setContentText(quoteText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quoteText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(!isPersistent)
            .setOngoing(isPersistent)
            .setColor(colorInt)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "quote_channel"
        const val NOTIFICATION_ID = 1
    }
}
