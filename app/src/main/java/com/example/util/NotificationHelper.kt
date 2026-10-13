package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    private const val CHANNEL_SOS = "accessos_emergency_sos"
    private const val CHANNEL_MEDICINE = "accessos_medicine_reminders"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val sosChannel = NotificationChannel(
                CHANNEL_SOS,
                "Emergency SOS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority emergency alerts for elder safety"
                enableVibration(true)
            }

            val medChannel = NotificationChannel(
                CHANNEL_MEDICINE,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily medicine schedule reminders"
            }

            notificationManager.createNotificationChannel(sosChannel)
            notificationManager.createNotificationChannel(medChannel)
        }
    }

    fun showSosDispatchedNotification(context: Context, triggeredBy: String, locationText: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_SOS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("🚨 EMERGENCY SOS BROADCAST")
                .setContentText("Emergency alert sent by $triggeredBy. Location: $locationText")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Emergency SOS alert broadcasted to family members and emergency contacts.\nLocation: $locationText")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(911, builder.build())
        } catch (_: Exception) {}
    }

    fun showMedicineNotification(context: Context, medicineName: String, time: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_MEDICINE)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("💊 Medicine Reminder: $time")
                .setContentText("Time to take $medicineName")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(medicineName.hashCode(), builder.build())
        } catch (_: Exception) {}
    }
}
