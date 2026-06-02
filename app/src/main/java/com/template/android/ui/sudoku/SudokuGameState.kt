package com.template.android.ui.sudoku

data class SudokuGameState(
    val puzzle: IntArray,
    val solution: IntArray,
    val userValues: IntArray,
    val fixed: BooleanArray,
    val notes: Array<MutableSet<Int>>,
    val difficulty: SudokuDifficulty,
    val selectedIndex: Int = -1,
    val notesMode: Boolean = false,
    val elapsedSeconds: Int = 0,
    val mistakes: Int = 0,
    val status: GameStatus = GameStatus.PLAYING,
) {
    companion object {
        fun newGame(puzzle: SudokuPuzzle): SudokuGameState {
            val fixed = BooleanArray(81) { puzzle.puzzle[it] != 0 }
            return SudokuGameState(
                puzzle = puzzle.puzzle,
                solution = puzzle.solution,
                userValues = puzzle.puzzle.copyOf(),
                fixed = fixed,
                notes = Array(81) { mutableSetOf() },
                difficulty = puzzle.difficulty,
            )
        }
    }

    fun withSelection(index: Int): SudokuGameState = copy(selectedIndex = index)

    fun tickSecond(): SudokuGameState =
        if (status == GameStatus.PLAYING) copy(elapsedSeconds = elapsedSeconds + 1) else this

    fun enterDigit(digit: Int): SudokuGameState {
        if (status != GameStatus.PLAYING || selectedIndex !in 0..80 || fixed[selectedIndex]) return this
        if (notesMode) {
            val newNotes = notes.copyOf()
            val cellNotes = newNotes[selectedIndex].toMutableSet()
            if (digit in cellNotes) {
                cellNotes.remove(digit)
            } else {
                if (isDigitSaturated(digit)) return this
                cellNotes.add(digit)
            }
            newNotes[selectedIndex] = cellNotes
            return copy(notes = newNotes)
        }
        if (!canEnterDigit(digit)) return this
        val updated = userValues.copyOf()
        val newNotes = notes.copyOf()
        newNotes[selectedIndex] = mutableSetOf()
        updated[selectedIndex] = digit
        val correct = digit == solution[selectedIndex]
        val newMistakes = if (!correct) mistakes + 1 else mistakes
        val won = correct && SudokuEngine.isComplete(updated, solution)
        return copy(
            userValues = updated,
            notes = newNotes,
            mistakes = newMistakes,
            status = if (won) GameStatus.WON else status,
        )
    }

    fun eraseCell(): SudokuGameState {
        if (status != GameStatus.PLAYING || selectedIndex !in 0..80 || fixed[selectedIndex]) return this
        val updated = userValues.copyOf()
        updated[selectedIndex] = 0
        val newNotes = notes.copyOf()
        newNotes[selectedIndex] = mutableSetOf()
        return copy(userValues = updated, notes = newNotes)
    }

    fun toggleNotesMode(): SudokuGameState = copy(notesMode = !notesMode)

    /**
     * Pista: rellena la celda seleccionada con el valor correcto.
     * Devuelve el nuevo estado y si se aplicó realmente.
     */
    fun applyHint(): Pair<SudokuGameState, Boolean> {
        if (status != GameStatus.PLAYING) return this to false
        if (selectedIndex !in 0..80) return this to false
        if (fixed[selectedIndex]) return this to false
        val correct = solution[selectedIndex]
        if (correct == 0) return this to false
        val before = userValues[selectedIndex]
        if (before == correct) return this to false

        val updated = userValues.copyOf()
        updated[selectedIndex] = correct
        val newNotes = notes.copyOf()
        newNotes[selectedIndex] = mutableSetOf()
        val won = SudokuEngine.isComplete(updated, solution)
        return copy(
            userValues = updated,
            notes = newNotes,
            status = if (won) GameStatus.WON else status,
        ) to true
    }

    fun isCellError(index: Int): Boolean {
        if (index !in 0..80 || fixed[index]) return false
        val value = userValues[index]
        return value != 0 && value != solution[index]
    }

    fun digitCount(digit: Int): Int =
        if (digit !in 1..9) 0 else userValues.count { it == digit }

    fun isDigitSaturated(digit: Int): Boolean = digitCount(digit) >= 9

    /** Permite volver a pulsar el mismo dígito en la celda seleccionada sin sumar otro. */
    fun canEnterDigit(digit: Int): Boolean {
        if (digit !in 1..9) return false
        if (!isDigitSaturated(digit)) return true
        return selectedIndex in 0..80 && userValues[selectedIndex] == digit
    }
}
