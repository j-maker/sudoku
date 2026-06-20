package com.template.android.data

import android.content.Context
import com.template.android.ui.sudoku.SudokuDifficulty
import com.template.android.util.LocaleHelper
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AppSettings(
    val languageCode: String = "es",
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 18,
    val soundEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val largeCells: Boolean = false,
    val highContrast: Boolean = false,
    val reduceMotion: Boolean = false,
    val highlightErrors: Boolean = true,
)

data class PlayerStats(
    val gamesWon: Int = 0,
    val gamesPlayed: Int = 0,
    val winStreak: Int = 0,
    val bestWinStreak: Int = 0,
    val winsEasy: Int = 0,
    val winsMedium: Int = 0,
    val winsHard: Int = 0,
    val lastPlayDate: String? = null,
    val consecutiveDays: Int = 0,
    val bestTimeEasy: Int? = null,
    val bestTimeMedium: Int? = null,
    val bestTimeHard: Int? = null,
    val dailyChallengeDate: String? = null,
    val dailyChallengeCompleted: Boolean = false,
)

data class HintState(
    val freeHintsRemaining: Int = 2,
    val lastRefillAtMs: Long = 0L,
)

class AppDataStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun ensureInitialLanguage(context: Context): String {
        if (prefs.getBoolean(K_LANGUAGE_AUTO_CONFIGURED, false)) {
            return resolvedLanguageCode()
        }
        if (prefs.all.isNotEmpty()) {
            prefs.edit().putBoolean(K_LANGUAGE_AUTO_CONFIGURED, true).commit()
            return resolvedLanguageCode()
        }
        val detected = LocaleHelper.languageCodeForSystem(context)
        prefs.edit()
            .putString(K_LANGUAGE, detected)
            .putBoolean(K_LANGUAGE_AUTO_CONFIGURED, true)
            .commit()
        return detected
    }

    fun loadSettings(): AppSettings = AppSettings(
        languageCode = resolvedLanguageCode(),
        dailyReminderEnabled = prefs.getBoolean(K_DAILY_REMINDER, false),
        dailyReminderHour = prefs.getInt(K_DAILY_REMINDER_HOUR, 18).coerceIn(6, 22),
        soundEnabled = prefs.getBoolean(K_SOUND, false),
        vibrationEnabled = prefs.getBoolean(K_VIBRATION, true),
        largeCells = prefs.getBoolean(K_LARGE_CELLS, false),
        highContrast = prefs.getBoolean(K_HIGH_CONTRAST, false),
        reduceMotion = prefs.getBoolean(K_REDUCE_MOTION, false),
        highlightErrors = prefs.getBoolean(K_HIGHLIGHT_ERRORS, true),
    )

    fun saveSettings(settings: AppSettings, sync: Boolean = false) {
        prefs.edit()
            .putString(K_LANGUAGE, settings.languageCode)
            .putBoolean(K_DAILY_REMINDER, settings.dailyReminderEnabled)
            .putInt(K_DAILY_REMINDER_HOUR, settings.dailyReminderHour.coerceIn(6, 22))
            .putBoolean(K_SOUND, settings.soundEnabled)
            .putBoolean(K_VIBRATION, settings.vibrationEnabled)
            .putBoolean(K_LARGE_CELLS, settings.largeCells)
            .putBoolean(K_HIGH_CONTRAST, settings.highContrast)
            .putBoolean(K_REDUCE_MOTION, settings.reduceMotion)
            .putBoolean(K_HIGHLIGHT_ERRORS, settings.highlightErrors)
            .apply()
        if (sync) {
            com.template.android.engagement.DailyReminderScheduler.sync(appContext)
        }
    }

    fun loadStats(): PlayerStats {
        if (!prefs.contains(K_WINS_EASY)) {
            migrateDifficultyWinCounts()
        }
        return PlayerStats(
            gamesWon = prefs.getInt(K_GAMES_WON, 0),
            gamesPlayed = prefs.getInt(K_GAMES_PLAYED, 0),
            winStreak = prefs.getInt(K_WIN_STREAK, 0),
            bestWinStreak = prefs.getInt(K_BEST_WIN_STREAK, 0),
            winsEasy = prefs.getInt(K_WINS_EASY, 0),
            winsMedium = prefs.getInt(K_WINS_MEDIUM, 0),
            winsHard = prefs.getInt(K_WINS_HARD, 0),
            lastPlayDate = prefs.getString(K_LAST_PLAY, null),
            consecutiveDays = prefs.getInt(K_CONSEC_DAYS, 0),
            bestTimeEasy = prefs.getInt(K_BEST_EASY, -1).takeIf { it >= 0 },
            bestTimeMedium = prefs.getInt(K_BEST_MEDIUM, -1).takeIf { it >= 0 },
            bestTimeHard = prefs.getInt(K_BEST_HARD, -1).takeIf { it >= 0 },
            dailyChallengeDate = prefs.getString(K_DAILY_DATE, null),
            dailyChallengeCompleted = prefs.getBoolean(K_DAILY_DONE, false),
        )
    }

    fun loadUnlockedAchievements(): Set<AchievementId> {
        val raw = prefs.getStringSet(K_ACHIEVEMENTS, emptySet()) ?: emptySet()
        return raw.mapNotNull { runCatching { AchievementId.valueOf(it) }.getOrNull() }.toSet()
    }

    fun unlockAchievement(id: AchievementId) {
        val current = loadUnlockedAchievements().toMutableSet()
        if (current.add(id)) {
            prefs.edit().putStringSet(K_ACHIEVEMENTS, current.map { it.name }.toSet()).apply()
        }
    }

    fun loadHintState(): HintState {
        val remaining = prefs.getInt(K_HINTS_FREE_REMAINING, 2).coerceIn(0, 2)
        return HintState(freeHintsRemaining = remaining, lastRefillAtMs = 0L)
    }

    fun saveHintState(state: HintState) {
        prefs.edit()
            .putInt(K_HINTS_FREE_REMAINING, state.freeHintsRemaining.coerceIn(0, 2))
            .putLong(K_HINTS_LAST_REFILL_MS, state.lastRefillAtMs)
            .apply()
    }

    fun saveHintRefillPaused(elapsedMs: Long) {
        prefs.edit()
            .putLong(K_HINTS_REFILL_ELAPSED_MS, elapsedMs.coerceAtLeast(0L))
            .apply()
    }

    fun loadHintRefillPausedElapsedMs(): Long? {
        val elapsed = prefs.getLong(K_HINTS_REFILL_ELAPSED_MS, -1L)
        return elapsed.takeIf { it >= 0L }
    }

    fun clearHintRefillPaused() {
        prefs.edit().remove(K_HINTS_REFILL_ELAPSED_MS).apply()
    }

    fun canShowRewardedAdToday(maxPerDay: Int = REWARDED_ADS_DAILY_MAX): Boolean {
        return rewardedAdsShownToday() < maxPerDay
    }

    fun recordRewardedAdShown(): Int {
        val today = todayString()
        val count = if (prefs.getString(K_REWARDED_ADS_DATE, null) == today) {
            prefs.getInt(K_REWARDED_ADS_COUNT, 0)
        } else {
            0
        } + 1
        prefs.edit()
            .putString(K_REWARDED_ADS_DATE, today)
            .putInt(K_REWARDED_ADS_COUNT, count)
            .apply()
        return count
    }

    fun rewardedAdsShownToday(): Int {
        val today = todayString()
        if (prefs.getString(K_REWARDED_ADS_DATE, null) != today) return 0
        return prefs.getInt(K_REWARDED_ADS_COUNT, 0)
    }

    fun wasReviewPrompted(): Boolean =
        prefs.getBoolean(K_REVIEW_PROMPTED, false) ||
            prefs.getBoolean(K_REVIEW_REQUESTED, false)

    fun setReviewPrompted() {
        prefs.edit().putBoolean(K_REVIEW_PROMPTED, true).apply()
    }

    /** Solo llamar tras una victoria; el prompt no debe mostrarse en derrotas. */
    fun shouldRequestReviewAfterWin(stats: PlayerStats): Boolean {
        if (wasReviewPrompted()) return false
        return stats.gamesWon >= REVIEW_AFTER_WINS ||
            (stats.dailyChallengeDate == todayString() && stats.dailyChallengeCompleted)
    }

    private fun migrateDifficultyWinCounts() {
        val hasMedium = prefs.getInt(K_BEST_MEDIUM, -1) >= 0
        val hasHard = prefs.getInt(K_BEST_HARD, -1) >= 0
        prefs.edit()
            .putInt(
                K_WINS_EASY,
                if (hasMedium || hasHard) {
                    com.template.android.ui.sudoku.SudokuDifficultyUnlock.WINS_EASY_FOR_MEDIUM
                } else {
                    0
                },
            )
            .putInt(
                K_WINS_MEDIUM,
                if (hasHard) {
                    com.template.android.ui.sudoku.SudokuDifficultyUnlock.WINS_MEDIUM_FOR_HARD
                } else {
                    0
                },
            )
            .putInt(K_WINS_HARD, 0)
            .apply()
    }

    fun saveStats(stats: PlayerStats) {
        prefs.edit()
            .putInt(K_GAMES_WON, stats.gamesWon)
            .putInt(K_GAMES_PLAYED, stats.gamesPlayed)
            .putInt(K_WIN_STREAK, stats.winStreak)
            .putInt(K_BEST_WIN_STREAK, stats.bestWinStreak)
            .putInt(K_WINS_EASY, stats.winsEasy)
            .putInt(K_WINS_MEDIUM, stats.winsMedium)
            .putInt(K_WINS_HARD, stats.winsHard)
            .putString(K_LAST_PLAY, stats.lastPlayDate)
            .putInt(K_CONSEC_DAYS, stats.consecutiveDays)
            .putInt(K_BEST_EASY, stats.bestTimeEasy ?: -1)
            .putInt(K_BEST_MEDIUM, stats.bestTimeMedium ?: -1)
            .putInt(K_BEST_HARD, stats.bestTimeHard ?: -1)
            .putString(K_DAILY_DATE, stats.dailyChallengeDate)
            .putBoolean(K_DAILY_DONE, stats.dailyChallengeCompleted)
            .apply()
    }

    fun recordWin(
        difficulty: SudokuDifficulty,
        elapsedSeconds: Int,
        mistakes: Int,
    ): GameEndResult {
        val today = todayString()
        val stats = loadStats()
        val newUnlocks = mutableListOf<AchievementId>()
        var beatBestTime = false
        var bestTimeDifficulty: SudokuDifficulty? = null
        val wasDailyDone = stats.dailyChallengeDate == today && stats.dailyChallengeCompleted

        val newStreak = stats.winStreak + 1
        val beforeBest = when (difficulty) {
            SudokuDifficulty.EASY -> stats.bestTimeEasy
            SudokuDifficulty.MEDIUM -> stats.bestTimeMedium
            SudokuDifficulty.HARD -> stats.bestTimeHard
        }
        var updated = stats.copy(
            gamesWon = stats.gamesWon + 1,
            gamesPlayed = stats.gamesPlayed + 1,
            winStreak = newStreak,
            bestWinStreak = maxOf(stats.bestWinStreak, newStreak),
            winsEasy = stats.winsEasy + if (difficulty == SudokuDifficulty.EASY) 1 else 0,
            winsMedium = stats.winsMedium + if (difficulty == SudokuDifficulty.MEDIUM) 1 else 0,
            winsHard = stats.winsHard + if (difficulty == SudokuDifficulty.HARD) 1 else 0,
            bestTimeEasy = updateBest(stats.bestTimeEasy, elapsedSeconds, difficulty, SudokuDifficulty.EASY),
            bestTimeMedium = updateBest(stats.bestTimeMedium, elapsedSeconds, difficulty, SudokuDifficulty.MEDIUM),
            bestTimeHard = updateBest(stats.bestTimeHard, elapsedSeconds, difficulty, SudokuDifficulty.HARD),
        )
        val afterBest = when (difficulty) {
            SudokuDifficulty.EASY -> updated.bestTimeEasy
            SudokuDifficulty.MEDIUM -> updated.bestTimeMedium
            SudokuDifficulty.HARD -> updated.bestTimeHard
        }
        if (afterBest != null && (beforeBest == null || afterBest < beforeBest)) {
            beatBestTime = true
            bestTimeDifficulty = difficulty
        }
        updated = updatePlayStreak(updated, today)
        updated = updateDailyChallenge(updated, today, difficulty, elapsedSeconds, mistakes)
        saveStats(updated)

        val unlocked = loadUnlockedAchievements()
        if (AchievementId.FIRST_WIN !in unlocked) {
            unlockAchievement(AchievementId.FIRST_WIN)
            newUnlocks.add(AchievementId.FIRST_WIN)
        }
        if (updated.gamesWon >= 10 && AchievementId.WINS_10 !in unlocked) {
            unlockAchievement(AchievementId.WINS_10)
            newUnlocks.add(AchievementId.WINS_10)
        }
        if (difficulty == SudokuDifficulty.HARD && AchievementId.WIN_HARD !in unlocked) {
            unlockAchievement(AchievementId.WIN_HARD)
            newUnlocks.add(AchievementId.WIN_HARD)
        }
        if (updated.winStreak >= 3 && AchievementId.STREAK_3 !in unlocked) {
            unlockAchievement(AchievementId.STREAK_3)
            newUnlocks.add(AchievementId.STREAK_3)
        }
        if (updated.consecutiveDays >= 7 && AchievementId.PLAY_7_DAYS !in unlocked) {
            unlockAchievement(AchievementId.PLAY_7_DAYS)
            newUnlocks.add(AchievementId.PLAY_7_DAYS)
        }
        if (updated.dailyChallengeDate == today && updated.dailyChallengeCompleted &&
            AchievementId.DAILY_CHALLENGE !in unlocked
        ) {
            unlockAchievement(AchievementId.DAILY_CHALLENGE)
            newUnlocks.add(AchievementId.DAILY_CHALLENGE)
        }

        val dailyJustCompleted = !wasDailyDone &&
            updated.dailyChallengeDate == today &&
            updated.dailyChallengeCompleted

        return GameEndResult(
            newUnlocks = newUnlocks,
            beatBestTime = beatBestTime,
            bestTimeDifficulty = bestTimeDifficulty,
            dailyChallengeJustCompleted = dailyJustCompleted,
        )
    }

    /** Actualiza la racha diaria al abrir la app o jugar (una vez por sesión en pantalla). */
    fun recordDailyPlay(): PlayerStats {
        val updated = updatePlayStreak(loadStats(), todayString())
        saveStats(updated)
        return updated
    }

    fun recordGameStarted() {
        val stats = loadStats()
        saveStats(stats.copy(gamesPlayed = stats.gamesPlayed + 1))
    }

    fun resetStats() {
        saveStats(PlayerStats())
        prefs.edit().remove(K_ACHIEVEMENTS).apply()
    }

    fun saveGame(json: String) {
        prefs.edit().putString(K_SAVED_GAME, json).apply()
    }

    fun loadSavedGame(): String? = prefs.getString(K_SAVED_GAME, null)

    fun clearSavedGame() {
        prefs.edit().remove(K_SAVED_GAME).apply()
    }

    /** Nueva partida desde el tablero (reiniciar / cambiar dificultad). No cuenta el botón del diálogo de victoria. */
    fun incrementBoardNewGameCount(): Int {
        val next = prefs.getInt(K_BOARD_NEW_GAME_COUNT, 0) + 1
        prefs.edit().putInt(K_BOARD_NEW_GAME_COUNT, next).apply()
        return next
    }

    fun isTutorialCompleted(): Boolean = prefs.getBoolean(K_TUTORIAL_DONE, false)

    fun setTutorialCompleted() {
        prefs.edit().putBoolean(K_TUTORIAL_DONE, true).apply()
    }

    fun isReminderPromptShown(): Boolean = prefs.getBoolean(K_REMINDER_PROMPT, false)

    fun setReminderPromptShown() {
        prefs.edit().putBoolean(K_REMINDER_PROMPT, true).apply()
    }

    private fun updateBest(
        current: Int?,
        elapsed: Int,
        actual: SudokuDifficulty,
        target: SudokuDifficulty,
    ): Int? {
        if (actual != target) return current
        return minOf(current ?: Int.MAX_VALUE, elapsed).takeIf { it != Int.MAX_VALUE }
    }

    private fun resolvedLanguageCode(): String =
        prefs.getString(K_LANGUAGE, "es") ?: "es"

    private fun todayString(): String = dateFormat.format(Calendar.getInstance().time)

    private fun updatePlayStreak(stats: PlayerStats, today: String): PlayerStats {
        val last = stats.lastPlayDate
        val consecutive = when (last) {
            null -> 1
            today -> stats.consecutiveDays.coerceAtLeast(1)
            else -> {
                val lastMs = parseDate(last)
                val todayMs = parseDate(today)
                if (lastMs != null && todayMs != null) {
                    val diffDays = TimeUnit.MILLISECONDS.toDays(todayMs - lastMs)
                    if (diffDays == 1L) stats.consecutiveDays + 1 else 1
                } else {
                    1
                }
            }
        }
        return stats.copy(lastPlayDate = today, consecutiveDays = consecutive)
    }

    private fun parseDate(value: String): Long? =
        runCatching { dateFormat.parse(value)?.time }.getOrNull()

    private fun updateDailyChallenge(
        stats: PlayerStats,
        today: String,
        difficulty: SudokuDifficulty,
        elapsedSeconds: Int,
        mistakes: Int,
    ): PlayerStats {
        var s = stats
        if (s.dailyChallengeDate != today) {
            s = s.copy(dailyChallengeDate = today, dailyChallengeCompleted = false)
        }
        if (!s.dailyChallengeCompleted) {
            val challenge = DailyChallengeRules.challengeForDate(today)
            if (DailyChallengeRules.isCompleted(challenge, true, difficulty, elapsedSeconds, mistakes)) {
                s = s.copy(dailyChallengeCompleted = true)
            }
        }
        return s
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private const val PREFS = "sudoku_game_data"

        fun gameToJson(state: com.template.android.ui.sudoku.SudokuGameState): String {
            val o = JSONObject()
            o.put("puzzle", intArrayToJson(state.puzzle))
            o.put("solution", intArrayToJson(state.solution))
            o.put("userValues", intArrayToJson(state.userValues))
            o.put("fixed", booleanArrayToJson(state.fixed))
            o.put("notes", notesToJson(state.notes))
            o.put("difficulty", state.difficulty.name)
            o.put("elapsedSeconds", state.elapsedSeconds)
            o.put("mistakes", state.mistakes)
            o.put("status", state.status.name)
            o.put("notesMode", state.notesMode)
            return o.toString()
        }

        fun gameFromJson(json: String): com.template.android.ui.sudoku.SudokuGameState? = try {
            val o = JSONObject(json)
            com.template.android.ui.sudoku.SudokuGameState(
                puzzle = jsonToIntArray(o.getJSONArray("puzzle")),
                solution = jsonToIntArray(o.getJSONArray("solution")),
                userValues = jsonToIntArray(o.getJSONArray("userValues")),
                fixed = jsonToBooleanArray(o.getJSONArray("fixed")),
                notes = jsonToNotes(o.getJSONArray("notes")),
                difficulty = SudokuDifficulty.valueOf(o.getString("difficulty")),
                elapsedSeconds = o.getInt("elapsedSeconds"),
                mistakes = o.getInt("mistakes"),
                status = com.template.android.ui.sudoku.GameStatus.valueOf(o.getString("status")),
                notesMode = o.optBoolean("notesMode", false),
            )
        } catch (_: Exception) {
            null
        }

        private fun intArrayToJson(arr: IntArray) = JSONArray().apply { arr.forEach { put(it) } }
        private fun booleanArrayToJson(arr: BooleanArray) = JSONArray().apply { arr.forEach { put(it) } }
        private fun notesToJson(notes: Array<MutableSet<Int>>) = JSONArray().apply {
            notes.forEach { set -> put(JSONArray().apply { set.sorted().forEach { put(it) } }) }
        }
        private fun jsonToIntArray(a: JSONArray) = IntArray(a.length()) { a.getInt(it) }
        private fun jsonToBooleanArray(a: JSONArray) = BooleanArray(a.length()) { a.getBoolean(it) }
        private fun jsonToNotes(a: JSONArray) = Array(a.length()) { i ->
            val inner = a.getJSONArray(i)
            (0 until inner.length()).map { inner.getInt(it) }.toMutableSet()
        }

        private const val K_LANGUAGE = "language"
        private const val K_LANGUAGE_AUTO_CONFIGURED = "language_auto"
        private const val K_DAILY_REMINDER = "daily_reminder"
        private const val K_DAILY_REMINDER_HOUR = "daily_reminder_hour"
        private const val K_SOUND = "sound_enabled"
        private const val K_VIBRATION = "vibration_enabled"
        private const val K_LARGE_CELLS = "large_cells"
        private const val K_HIGH_CONTRAST = "high_contrast"
        private const val K_REDUCE_MOTION = "reduce_motion"
        private const val K_HIGHLIGHT_ERRORS = "highlight_errors"
        private const val K_WINS_EASY = "wins_easy"
        private const val K_WINS_MEDIUM = "wins_medium"
        private const val K_WINS_HARD = "wins_hard"
        private const val K_LAST_PLAY = "last_play_date"
        private const val K_CONSEC_DAYS = "consecutive_days"
        private const val K_GAMES_WON = "games_won"
        private const val K_GAMES_PLAYED = "games_played"
        private const val K_WIN_STREAK = "win_streak"
        private const val K_BEST_WIN_STREAK = "best_win_streak"
        private const val K_BEST_EASY = "best_time_easy"
        private const val K_BEST_MEDIUM = "best_time_medium"
        private const val K_BEST_HARD = "best_time_hard"
        private const val K_SAVED_GAME = "saved_game"
        private const val K_BOARD_NEW_GAME_COUNT = "board_new_game_count"
        private const val K_TUTORIAL_DONE = "tutorial_done"
        private const val K_REMINDER_PROMPT = "reminder_prompt_shown"
        private const val K_HINTS_FREE_REMAINING = "hints_free_remaining"
        private const val K_HINTS_LAST_REFILL_MS = "hints_last_refill_ms"
        private const val K_HINTS_REFILL_ELAPSED_MS = "hints_refill_elapsed_ms"
        private const val K_REWARDED_ADS_DATE = "rewarded_ads_date"
        private const val K_REWARDED_ADS_COUNT = "rewarded_ads_count"
        private const val K_REVIEW_PROMPTED = "review_prompted"
        private const val K_REVIEW_REQUESTED = "review_requested"
        private const val K_DAILY_DATE = "daily_date"
        private const val K_DAILY_DONE = "daily_done"
        private const val K_ACHIEVEMENTS = "achievements"

        const val REWARDED_ADS_DAILY_MAX = 12
        const val REVIEW_AFTER_WINS = 3
    }
}
