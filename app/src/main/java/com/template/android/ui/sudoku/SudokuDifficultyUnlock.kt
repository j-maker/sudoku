package com.template.android.ui.sudoku

import com.template.android.data.PlayerStats

object SudokuDifficultyUnlock {
    const val WINS_EASY_FOR_MEDIUM = 3
    const val WINS_MEDIUM_FOR_HARD = 10

    fun PlayerStats.isDifficultyUnlocked(difficulty: SudokuDifficulty): Boolean = when (difficulty) {
        SudokuDifficulty.EASY -> true
        SudokuDifficulty.MEDIUM -> winsEasy >= WINS_EASY_FOR_MEDIUM
        SudokuDifficulty.HARD -> winsMedium >= WINS_MEDIUM_FOR_HARD
    }

    fun PlayerStats.highestUnlockedDifficulty(): SudokuDifficulty = when {
        isDifficultyUnlocked(SudokuDifficulty.HARD) -> SudokuDifficulty.HARD
        isDifficultyUnlocked(SudokuDifficulty.MEDIUM) -> SudokuDifficulty.MEDIUM
        else -> SudokuDifficulty.EASY
    }

    fun PlayerStats.winsTowardUnlock(difficulty: SudokuDifficulty): Pair<Int, Int>? = when (difficulty) {
        SudokuDifficulty.EASY -> null
        SudokuDifficulty.MEDIUM -> winsEasy to WINS_EASY_FOR_MEDIUM
        SudokuDifficulty.HARD -> winsMedium to WINS_MEDIUM_FOR_HARD
    }
}
