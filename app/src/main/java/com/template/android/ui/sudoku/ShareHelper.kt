package com.template.android.ui.sudoku

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

const val PLAY_STORE_APP_ID = "com.jmaker.sudoku"

enum class SocialPlatform {
    WHATSAPP,
    FACEBOOK,
    INSTAGRAM,
    TWITTER,
    TELEGRAM,
    MORE,
}

data class ShareStrings(
    val subject: String,
    val chooserTitle: String,
    val appNotInstalled: String,
    val instagramPaste: String,
)

object ShareHelper {

    private val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")
    private val FACEBOOK_PACKAGES = listOf("com.facebook.katana", "com.facebook.lite")
    private val INSTAGRAM_PACKAGES = listOf("com.instagram.android")
    private val TWITTER_PACKAGES = listOf("com.twitter.android", "com.x.android")
    private val TELEGRAM_PACKAGES = listOf("org.telegram.messenger", "org.telegram.messenger.web")

    fun playStoreUrl(): String =
        "https://play.google.com/store/apps/details?id=$PLAY_STORE_APP_ID"

    fun share(context: Context, platform: SocialPlatform, message: String, strings: ShareStrings) {
        when (platform) {
            SocialPlatform.WHATSAPP -> shareWhatsApp(context, message, strings)
            SocialPlatform.TELEGRAM -> shareTelegram(context, message, strings)
            SocialPlatform.TWITTER -> shareTwitter(context, message, strings)
            SocialPlatform.FACEBOOK -> shareFacebook(context, message, strings)
            SocialPlatform.INSTAGRAM -> shareInstagram(context, message, strings)
            SocialPlatform.MORE -> openChooser(context, message, strings)
        }
    }

    private fun shareWhatsApp(context: Context, message: String, strings: ShareStrings) {
        if (trySendTextInPackages(context, message, WHATSAPP_PACKAGES, strings)) return
        val deepLinks = listOf(
            "whatsapp://send?text=${Uri.encode(message)}",
            "https://api.whatsapp.com/send?text=${Uri.encode(message)}",
        )
        if (tryViewLinksInPackages(context, deepLinks, WHATSAPP_PACKAGES)) return
        fallback(context, message, WHATSAPP_PACKAGES, strings)
    }

    private fun shareTelegram(context: Context, message: String, strings: ShareStrings) {
        if (trySendTextInPackages(context, message, TELEGRAM_PACKAGES, strings)) return
        val url = playStoreUrl()
        val deepLinks = listOf(
            "tg://msg?text=${Uri.encode(message)}",
            "https://t.me/share/url?url=${Uri.encode(url)}&text=${Uri.encode(message)}",
        )
        if (tryViewLinksInPackages(context, deepLinks, TELEGRAM_PACKAGES)) return
        fallback(context, message, TELEGRAM_PACKAGES, strings)
    }

    private fun shareTwitter(context: Context, message: String, strings: ShareStrings) {
        if (trySendTextInPackages(context, message, TWITTER_PACKAGES, strings)) return
        val deepLinks = listOf(
            "twitter://post?message=${Uri.encode(message)}",
            "https://twitter.com/intent/tweet?text=${Uri.encode(message)}",
        )
        if (tryViewLinksInPackages(context, deepLinks, TWITTER_PACKAGES)) return
        fallback(context, message, TWITTER_PACKAGES, strings)
    }

    private fun shareFacebook(context: Context, message: String, strings: ShareStrings) {
        if (trySendTextInPackages(context, message, FACEBOOK_PACKAGES, strings)) return
        val url = playStoreUrl()
        val deepLinks = listOf(
            "https://www.facebook.com/sharer/sharer.php?u=${Uri.encode(url)}&quote=${Uri.encode(message)}",
        )
        if (tryViewLinksInPackages(context, deepLinks, FACEBOOK_PACKAGES)) return
        fallback(context, message, FACEBOOK_PACKAGES, strings)
    }

    private fun shareInstagram(context: Context, message: String, strings: ShareStrings) {
        if (trySendTextInPackages(context, message, INSTAGRAM_PACKAGES, strings)) return
        copyToClipboard(context, message)
        for (packageName in INSTAGRAM_PACKAGES) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: continue
            Toast.makeText(context, strings.instagramPaste, Toast.LENGTH_LONG).show()
            startShareActivity(context, launchIntent)
            return
        }
        fallback(context, message, INSTAGRAM_PACKAGES, strings)
    }

    private fun tryViewLinksInPackages(
        context: Context,
        links: List<String>,
        packages: List<String>,
    ): Boolean {
        for (link in links) {
            val uri = Uri.parse(link)
            for (packageName in packages) {
                if (tryStart(context, Intent(Intent.ACTION_VIEW, uri).setPackage(packageName))) return true
            }
            if (tryStart(context, Intent(Intent.ACTION_VIEW, uri))) return true
        }
        return false
    }

    private fun trySendTextInPackages(
        context: Context,
        message: String,
        packages: List<String>,
        strings: ShareStrings,
    ): Boolean {
        for (packageName in packages) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, strings.subject)
                putExtra(Intent.EXTRA_TEXT, message)
                setPackage(packageName)
            }
            if (tryStart(context, intent)) return true
        }
        return false
    }

    private fun fallback(context: Context, message: String, packages: List<String>, strings: ShareStrings) {
        Toast.makeText(context, strings.appNotInstalled, Toast.LENGTH_SHORT).show()
        openChooser(context, message, strings, packages.firstOrNull())
    }

    private fun openChooser(
        context: Context,
        message: String,
        strings: ShareStrings,
        preferredPackage: String? = null,
    ) {
        val chooser = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, strings.subject)
                putExtra(Intent.EXTRA_TEXT, message)
                preferredPackage?.let { setPackage(it) }
            },
            strings.chooserTitle,
        )
        if (!tryStart(context, chooser)) {
            Toast.makeText(context, strings.appNotInstalled, Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("share", text))
    }

    private fun tryStart(context: Context, intent: Intent): Boolean {
        return try {
            startShareActivity(context, intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    private fun startShareActivity(context: Context, intent: Intent) {
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
