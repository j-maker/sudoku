package com.template.android.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.template.android.R

object PrivacyHelper {

    fun openPrivacyPolicy(context: Context) {
        val url = context.getString(R.string.privacy_policy_url)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
