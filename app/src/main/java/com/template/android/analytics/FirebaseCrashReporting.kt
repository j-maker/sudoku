package com.template.android.analytics

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.template.android.BuildConfig

object FirebaseCrashReporting {

    fun init(context: Context) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        crashlytics.setCustomKey("app_slug", BuildConfig.APP_SLUG)
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("app_package", context.packageName)
    }

    fun recordException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}
