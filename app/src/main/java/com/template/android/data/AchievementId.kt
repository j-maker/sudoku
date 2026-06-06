package com.template.android.data

enum class AchievementId {
    FIRST_WIN,
    WINS_10,
    WIN_HARD,
    STREAK_3,
    PLAY_7_DAYS,
    DAILY_CHALLENGE,
}

data class GameEndResult(
    val newUnlocks: List<AchievementId>,
    val beatBestTime: Boolean,
    val bestTimeDifficulty: com.template.android.ui.sudoku.SudokuDifficulty?,
    val dailyChallengeJustCompleted: Boolean,
)
