package app.cashjournal.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.cashjournal.MainActivity
import app.cashjournal.R
import app.cashjournal.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = UserPreferencesRepository(context.applicationContext)
                val prefs = repository.userPreferencesFlow.first()

                if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
                    if (prefs.dailyReminderEnabled) {
                        DailyReminderScheduler.scheduleDailyReminder(context.applicationContext)
                    }
                    return@launch
                }

                if (!prefs.dailyReminderEnabled) {
                    return@launch
                }

                showDailyReminderNotification(context.applicationContext, prefs.language)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showDailyReminderNotification(context: Context, language: String) {
        val channelId = CHANNEL_ID
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = if (language.equals("in", ignoreCase = true)) "Pengingat Harian" else "Daily Reminder"
            val channelDesc = if (language.equals("in", ignoreCase = true)) {
                "Pengingat malam untuk mencatat pengeluaran dan pemasukan harian"
            } else {
                "Evening reminders to log your daily cash expenses and income"
            }
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = channelDesc
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isIndonesian = language.equals("in", ignoreCase = true)
        val title = if (isIndonesian) "Catat Transaksi Hari Ini" else "Record Today's Transactions"
        val message = if (isIndonesian) {
            "Sempatkan sejenak untuk mencatat pengeluaran dan pemasukan tunai hari ini."
        } else {
            "Take a moment to record today's cash expenses and income."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Notification permission might not be granted by user on Android 13+
        }
    }

    companion object {
        const val CHANNEL_ID = "cashjournal_daily_reminder"
        const val NOTIFICATION_ID = 2001
    }
}
