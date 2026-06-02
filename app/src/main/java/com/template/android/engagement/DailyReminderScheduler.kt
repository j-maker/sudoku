package com.template.android.engagement

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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.template.android.MainActivity
import com.template.android.R
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AppDataStore
import com.template.android.util.LocaleHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DailyReminderScheduler {

    private const val WORK_NAME = "daily_reminder"
    const val CHANNEL_ID = "app_reminders"
    const val REMINDER_NOTIFICATION_ID = 1001
    private const val REMINDER_REQUEST_CODE = 1001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }

    fun sync(context: Context) {
        val settings = AppDataStore(context).loadSettings()
        if (!settings.dailyReminderEnabled) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            return
        }
        ensureChannel(context)
        val initialDelay = millisUntilHour(settings.dailyReminderHour)
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
        AppAnalytics.logDailyReminderEnabled(true)
    }

    /** Quita el recordatorio del panel y el contador del icono al abrir la app. */
    fun clearReminderNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(REMINDER_NOTIFICATION_ID)
    }

    internal fun openAppPendingIntent(context: Context): PendingIntent {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_FROM_DAILY_REMINDER, true)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        return PendingIntent.getActivity(
            context,
            REMINDER_REQUEST_CODE,
            launchIntent,
            flags,
        )
    }

    private fun millisUntilHour(targetHour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    }

    const val EXTRA_FROM_DAILY_REMINDER = "from_daily_reminder"
}

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters,
) : Worker(context, params) {

    override fun doWork(): Result {
        val appContext = applicationContext
        val dataStore = AppDataStore(appContext)
        val settings = dataStore.loadSettings()
        if (!settings.dailyReminderEnabled) return Result.success()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return Result.success()
        }

        val localized = LocaleHelper.wrap(appContext, settings.languageCode)
        DailyReminderScheduler.ensureChannel(localized)
        val contentIntent = DailyReminderScheduler.openAppPendingIntent(localized)
        val notification = NotificationCompat.Builder(localized, DailyReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(localized.getString(R.string.notification_daily_title))
            .setContentText(localized.getString(R.string.notification_daily_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(localized).notify(
            DailyReminderScheduler.REMINDER_NOTIFICATION_ID,
            notification,
        )
        return Result.success()
    }
}
