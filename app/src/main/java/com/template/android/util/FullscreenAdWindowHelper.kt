package com.template.android.util

import android.app.Activity
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.template.android.R

/**
 * Evita que el contenido edge-to-edge asome por encima del vídeo en pantallas altas.
 */
object FullscreenAdWindowHelper {
    private var activeCount = 0

    fun enter(activity: Activity) {
        activeCount++
        if (activeCount > 1) return
        val window = activity.window
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun exit(activity: Activity) {
        if (activeCount <= 0) return
        activeCount--
        if (activeCount > 0) return
        val window = activity.window
        val background = activity.getColor(R.color.splash_background)
        window.statusBarColor = background
        window.navigationBarColor = background
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
