package com.template.android.engagement

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.template.android.MainActivity
import com.template.android.R
import com.template.android.data.AppDataStore
import com.template.android.data.DailyChallengeRules
import com.template.android.util.LocaleHelper
import java.util.Calendar

object DailyReminderScheduler {

    private const val LEGACY_WORK_NAME = "daily_reminder"
    const val CHANNEL_ID = "app_reminders"
    const val REMINDER_NOTIFICATION_ID = 1001
    private const val REMINDER_REQUEST_CODE = 1001
    const val ACTION_FIRE = "com.template.android.action.DAILY_REMINDER"
    const val EXTRA_FROM_DAILY_REMINDER = "from_daily_reminder"

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
        val appContext = context.applicationContext
        runCatching { WorkManager.getInstance(appContext).cancelUniqueWork(LEGACY_WORK_NAME) }
        val settings = AppDataStore(appContext).loadSettings()
        if (!settings.dailyReminderEnabled) {
            cancelAlarm(appContext)
            return
        }
        ensureChannel(appContext)
        scheduleNextAlarm(appContext, settings.dailyReminderHour)
    }

    fun clearReminderNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(REMINDER_NOTIFICATION_ID)
    }

    fun canPostNotifications(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    internal fun openAppPendingIntent(context: Context): PendingIntent {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_FROM_DAILY_REMINDER, true)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(
            context,
            REMINDER_REQUEST_CODE,
            launchIntent,
            flags,
        )
    }

    internal fun showIfDue(context: Context) {
        val appContext = context.applicationContext
        val dataStore = AppDataStore(appContext)
        val settings = dataStore.loadSettings()
        if (!settings.dailyReminderEnabled) return
        if (!canPostNotifications(appContext)) return

        val today = DailyChallengeRules.todayKey()
        if (dataStore.lastReminderNotifiedDate() == today) return

        val hourNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hourNow < settings.dailyReminderHour) return

        val localized = LocaleHelper.wrap(appContext, settings.languageCode)
        ensureChannel(localized)
        val dailyInfo = DailyChallengeRules.infoFor(dataStore.loadStats())
        val title: String
        val body: String
        if (dailyInfo.completed) {
            title = localized.getString(R.string.notification_play_title)
            body = localized.getString(R.string.notification_play_body)
        } else {
            val challengeTitle = localized.getString(DailyChallengeRules.titleRes(dailyInfo.type))
            title = localized.getString(R.string.notification_daily_title)
            body = localized.getString(R.string.notification_daily_body, challengeTitle)
        }
        val notification = NotificationCompat.Builder(localized, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent(localized))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(localized).notify(REMINDER_NOTIFICATION_ID, notification)
        dataStore.setLastReminderNotifiedDate(today)
    }

    private fun scheduleNextAlarm(context: Context, targetHour: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerAt = nextTriggerMillis(targetHour)
        val pending = alarmPendingIntent(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.cancel(alarmPendingIntent(context))
    }

    private fun alarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailyReminderReceiver::class.java).setAction(ACTION_FIRE)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, REMINDER_REQUEST_CODE, intent, flags)
    }

    private fun nextTriggerMillis(targetHour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            DailyReminderScheduler.ACTION_FIRE -> {
                DailyReminderScheduler.showIfDue(context)
                DailyReminderScheduler.sync(context)
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> DailyReminderScheduler.sync(context)
        }
    }
}
