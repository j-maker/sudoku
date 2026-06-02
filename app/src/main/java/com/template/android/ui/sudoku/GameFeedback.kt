package com.template.android.ui.sudoku

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.template.android.data.AppSettings

class GameFeedback(context: Context) {
    private val appContext = context.applicationContext
    private var settings = AppSettings()
    private var toneGenerator: ToneGenerator? = null

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = appContext.getSystemService(VibratorManager::class.java)
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Vibrator::class.java)
    }

    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
    }

    fun onCellFilled(correct: Boolean) {
        if (correct) playTone(ToneGenerator.TONE_PROP_BEEP, 40) else onError()
    }

    fun onError() {
        vibrate(30L)
        playTone(ToneGenerator.TONE_PROP_NACK, 60)
    }

    fun onWin() {
        vibrate(80L)
        playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 120)
    }

    fun onTap() {
        playTone(ToneGenerator.TONE_PROP_ACK, 25)
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }

    private fun playTone(tone: Int, durationMs: Int) {
        if (!settings.soundEnabled) return
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
            }
            toneGenerator?.startTone(tone, durationMs)
        } catch (_: Exception) {
        }
    }

    private fun vibrate(ms: Long) {
        if (!settings.vibrationEnabled) return
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(ms)
        }
    }
}
