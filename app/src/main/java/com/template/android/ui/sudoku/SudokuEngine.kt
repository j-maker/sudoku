package com.template.android.ui.sudoku

import kotlin.random.Random

object SudokuEngine {

    fun generate(difficulty: SudokuDifficulty, random: Random = Random.Default): SudokuPuzzle {
        val solution = IntArray(81)
        fillDiagonalBoxes(solution, random)
        solve(solution)
        val puzzle = solution.copyOf()
        removeCells(puzzle, 81 - difficulty.clues, random)
        return SudokuPuzzle(
            puzzle = puzzle,
            solution = solution,
            difficulty = difficulty,
        )
    }

    fun isValidPlacement(grid: IntArray, index: Int, value: Int): Boolean {
        if (value == 0) return true
        val row = index / 9
        val col = index % 9
        for (c in 0 until 9) {
            if (c != col && grid[row * 9 + c] == value) return false
        }
        for (r in 0 until 9) {
            if (r != row && grid[r * 9 + col] == value) return false
        }
        val boxRow = row / 3 * 3
        val boxCol = col / 3 * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                val i = r * 9 + c
                if (i != index && grid[i] == value) return false
            }
        }
        return true
    }

    fun isComplete(userGrid: IntArray, solution: IntArray): Boolean =
        userGrid.indices.all { userGrid[it] == solution[it] }

    private fun fillDiagonalBoxes(grid: IntArray, random: Random) {
        for (box in 0 until 3) {
            val nums = (1..9).shuffled(random)
            var n = 0
            val start = box * 3
            for (r in start until start + 3) {
                for (c in start until start + 3) {
                    grid[r * 9 + c] = nums[n++]
                }
            }
        }
    }

    private fun solve(grid: IntArray): Boolean {
        for (i in grid.indices) {
            if (grid[i] == 0) {
                for (num in 1..9) {
                    if (isValidPlacement(grid, i, num)) {
                        grid[i] = num
                        if (solve(grid)) return true
                        grid[i] = 0
                    }
                }
                return false
            }
        }
        return true
    }

    private fun removeCells(grid: IntArray, count: Int, random: Random) {
        val indices = grid.indices.shuffled(random).take(count.coerceAtMost(81))
        indices.forEach { grid[it] = 0 }
    }
}

data class SudokuPuzzle(
    val puzzle: IntArray,
    val solution: IntArray,
    val difficulty: SudokuDifficulty,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SudokuPuzzle) return false
        return puzzle.contentEquals(other.puzzle) &&
            solution.contentEquals(other.solution) &&
            difficulty == other.difficulty
    }

    override fun hashCode(): Int = puzzle.contentHashCode()
}
