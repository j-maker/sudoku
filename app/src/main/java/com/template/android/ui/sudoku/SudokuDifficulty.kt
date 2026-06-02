package com.template.android.ui.sudoku

enum class SudokuDifficulty(val clues: Int) {
    EASY(38),
    MEDIUM(30),
    HARD(22),
}

enum class GameStatus {
    PLAYING,
    WON,
}
