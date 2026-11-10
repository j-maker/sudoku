package com.template.android.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.template.android.R

object PrivacyHelper {

    fun openPrivacyPolicy(context: Context) {
        openUrl(context, context.getString(R.string.privacy_policy_url))
    }

    fun openTermsOfService(context: Context) {
        openUrl(context, context.getString(R.string.terms_of_service_url))
    }

    private fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
