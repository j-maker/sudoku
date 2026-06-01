package com.template.android

import android.app.Application
import com.template.android.analytics.AppAnalytics
import com.template.android.engagement.DailyReminderScheduler
import com.template.android.engagement.PlayGamesHelper
import com.template.android.security.PlayIntegrityChecker

class TemplateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppAnalytics.init(this)
        PlayIntegrityChecker.checkOnAppStart(this)
        PlayGamesHelper.initialize(this)
        DailyReminderScheduler.ensureChannel(this)
        DailyReminderScheduler.sync(this)
    }
}
