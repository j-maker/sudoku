package com.template.android.data

import com.template.android.R
import com.template.android.ui.sudoku.SudokuDifficulty
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class DailyChallengeType {
    EASY_UNDER_300,
    WIN_MEDIUM,
    WIN_HARD,
    EASY_NO_MISTAKES,
    MEDIUM_UNDER_600,
}

data class DailyChallengeInfo(
    val type: DailyChallengeType,
    val dateKey: String,
    val completed: Boolean,
)

object DailyChallengeRules {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun todayKey(): String = dateFormat.format(Calendar.getInstance().time)

    fun challengeForDate(dateKey: String): DailyChallengeType {
        val parsed = runCatching { dateFormat.parse(dateKey)?.time }.getOrNull()
        val dayIndex = if (parsed != null) {
            TimeUnit.MILLISECONDS.toDays(parsed)
        } else {
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        }
        val types = DailyChallengeType.entries
        return types[(dayIndex % types.size).toInt().coerceAtLeast(0)]
    }

    fun infoFor(stats: PlayerStats): DailyChallengeInfo {
        val today = todayKey()
        val dateKey = stats.dailyChallengeDate?.takeIf { it == today } ?: today
        val completed = stats.dailyChallengeDate == today && stats.dailyChallengeCompleted
        return DailyChallengeInfo(
            type = challengeForDate(dateKey),
            dateKey = dateKey,
            completed = completed,
        )
    }

    fun titleRes(type: DailyChallengeType): Int = when (type) {
        DailyChallengeType.EASY_UNDER_300 -> R.string.daily_challenge_easy_300
        DailyChallengeType.WIN_MEDIUM -> R.string.daily_challenge_win_medium
        DailyChallengeType.WIN_HARD -> R.string.daily_challenge_win_hard
        DailyChallengeType.EASY_NO_MISTAKES -> R.string.daily_challenge_easy_no_mistakes
        DailyChallengeType.MEDIUM_UNDER_600 -> R.string.daily_challenge_medium_600
    }

    fun isCompleted(
        type: DailyChallengeType,
        won: Boolean,
        difficulty: SudokuDifficulty,
        elapsedSeconds: Int,
        mistakes: Int,
    ): Boolean {
        if (!won) return false
        return when (type) {
            DailyChallengeType.EASY_UNDER_300 ->
                difficulty == SudokuDifficulty.EASY && elapsedSeconds <= 300
            DailyChallengeType.WIN_MEDIUM ->
                difficulty == SudokuDifficulty.MEDIUM
            DailyChallengeType.WIN_HARD ->
                difficulty == SudokuDifficulty.HARD
            DailyChallengeType.EASY_NO_MISTAKES ->
                difficulty == SudokuDifficulty.EASY && mistakes == 0
            DailyChallengeType.MEDIUM_UNDER_600 ->
                difficulty == SudokuDifficulty.MEDIUM && elapsedSeconds <= 600
        }
    }
}
