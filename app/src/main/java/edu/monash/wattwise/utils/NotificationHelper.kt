package edu.monash.wattwise.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import edu.monash.wattwise.MainActivity
import edu.monash.wattwise.R
import edu.monash.wattwise.constants.Reminders

/** Shows the app's notifications. Used by the reminder alarm and by the background worker. */
object NotificationHelper {

    /**
     * Shows a notification that opens the app when tapped. Returns false when
     * the user has not allowed notifications, so the caller can log it.
     */
    fun show(context: Context, notificationId: Int, title: String, text: String): Boolean {
        createNotificationChannel(context)

        // From Android 13 the user must allow notifications before one can be shown.
        val permissionMissing = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (permissionMissing) return false

        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, Reminders.CHANNEL_ID)
            .setSmallIcon(R.drawable.live_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
        return true
    }

    /** Notifications need a channel from Android 8. Creating it again does nothing. */
    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            Reminders.CHANNEL_ID,
            Reminders.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
