package com.template.android.ui.sudoku

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import android.app.Activity
import android.widget.Toast
import com.template.android.data.PlayerStats
import com.template.android.ui.sudoku.SudokuDifficultyUnlock.highestUnlockedDifficulty
import com.template.android.ui.sudoku.SudokuDifficultyUnlock.isDifficultyUnlocked
import com.template.android.ui.sudoku.SudokuDifficultyUnlock.winsTowardUnlock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.android.R
import com.template.android.ads.BannerAdView
import com.template.android.ads.InterstitialAdManager
import com.template.android.ads.MobileAdsInitializer
import com.template.android.ads.RewardedAdManager
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AppDataStore
import com.template.android.data.AppSettings
import com.template.android.data.DailyChallengeRules
import com.template.android.data.HintState
import com.template.android.engagement.PlayGamesHelper
import com.template.android.engagement.PlayReviewHelper
import com.template.android.ui.layout.SudokuLayoutInfo
import com.template.android.ui.layout.distributedBoardMaxHeight
import com.template.android.ui.layout.rememberSudokuLayoutInfo
import com.template.android.ui.theme.SudokuAccent
import com.template.android.ui.theme.SudokuBackgroundEnd
import com.template.android.ui.theme.SudokuBackgroundMid
import com.template.android.ui.theme.SudokuBackgroundStart
import com.template.android.ui.theme.SudokuChromeBg
import com.template.android.ui.theme.SudokuNeonPink
import com.template.android.ui.theme.SudokuPrimary
import com.template.android.util.FullscreenAdWindowHelper
import kotlinx.coroutines.delay

@Composable
fun SudokuScreen(
    dataStore: AppDataStore,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onOpenSettings: () -> Unit,
    interstitialAdManager: InterstitialAdManager?,
    openFromDailyReminder: Boolean = false,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val playGamesLeaderboardsEnabled = remember(context) { PlayGamesHelper.hasLeaderboards(context) }
    val leaderboardLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { /* Play Games UI; no result needed */ }

    fun openPlayGamesLeaderboards(preferredDifficulty: SudokuDifficulty? = null) {
        val act = activity ?: return
        PlayGamesHelper.showAllLeaderboards(
            activity = act,
            preferredDifficulty = preferredDifficulty,
            startLeaderboardUi = { leaderboardLauncher.launch(it) },
        )
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val feedback = remember { GameFeedback(context) }
    val rewardedAds = remember { RewardedAdManager(context) }

    var playerStats by remember { mutableStateOf(dataStore.loadStats()) }
    var gameState by remember {
        mutableStateOf(loadInitialGameState(dataStore, playerStats))
    }
    var showStats by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }
    var showWin by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var hintState by remember {
        mutableStateOf(dataStore.loadHintState().copy(lastRefillAtMs = 0L))
    }
    var earnedHints by remember { mutableStateOf(0) }
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var interstitialActive by remember { mutableStateOf(false) }
    var appInForeground by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    var pendingReviewAfterWin by remember { mutableStateOf(false) }
    var lastFullscreenAdEndMs by remember { mutableLongStateOf(0L) }
    var achievementToasts by remember { mutableStateOf<List<String>>(emptyList()) }
    var shareFromVictory by remember { mutableStateOf(false) }
    var victoryProcessed by remember { mutableStateOf(false) }
    val dailyChallengeInfo = remember(playerStats) { DailyChallengeRules.infoFor(playerStats) }

    fun beginFullscreenAd() {
        interstitialActive = true
        activity?.let { FullscreenAdWindowHelper.enter(it) }
        interstitialAdManager?.pauseClock()
    }

    fun endFullscreenAd() {
        interstitialActive = false
        activity?.let { FullscreenAdWindowHelper.exit(it) }
        lastFullscreenAdEndMs = System.currentTimeMillis()
        interstitialAdManager?.resumeClock()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    appInForeground = false
                    interstitialAdManager?.pauseClock()
                }
                Lifecycle.Event.ON_RESUME -> {
                    appInForeground = true
                    interstitialAdManager?.resumeClock()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val audioEnabled = settings.soundEnabled
    val freeHintsMax = 2
    val freeHintsRemaining = hintState.freeHintsRemaining

    fun consumeFreeHintIfNeeded(applied: Boolean) {
        if (!applied) return
        val remainingAfter = (hintState.freeHintsRemaining - 1).coerceAtLeast(0)
        val updated = hintState.copy(
            freeHintsRemaining = remainingAfter,
            lastRefillAtMs = 0L,
        )
        hintState = updated
        dataStore.saveHintState(updated)
    }

    fun applyHintWithEffects(): Boolean {
        val (next, applied) = gameState.applyHint()
        if (!applied) {
            Toast.makeText(context, context.getString(R.string.hint_no_moves), Toast.LENGTH_SHORT).show()
            return false
        }
        gameState = next
        feedback.onCellFilled(true)
        return true
    }

    fun onHintClick() {
        if (freeHintsRemaining > 0) {
            val success = applyHintWithEffects()
            AppAnalytics.logHintUsed("free", success)
            if (success) consumeFreeHintIfNeeded(true)
            return
        }
        if (earnedHints > 0) {
            val success = applyHintWithEffects()
            AppAnalytics.logHintUsed("earned", success)
            if (success) {
                earnedHints = (earnedHints - 1).coerceAtLeast(0)
            }
            return
        }

        if (!dataStore.canShowRewardedAdToday()) {
            AppAnalytics.logHintVideoBlocked("daily_limit")
            Toast.makeText(
                context,
                context.getString(R.string.hint_video_daily_limit),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        val act = context as? Activity ?: return
        val shown = rewardedAds.showIfReady(
            activity = act,
            onAdStarted = { beginFullscreenAd() },
            onRewardEarned = {
                earnedHints = 1
                dataStore.recordRewardedAdShown()
                interstitialAdManager?.markRewardedShown()
                AppAnalytics.logHintUsed("video_reward", true)
            },
            onFinished = { endFullscreenAd() },
        )
        if (!shown) {
            AppAnalytics.logHintVideoBlocked("ad_not_ready")
            Toast.makeText(context, context.getString(R.string.hint_ad_loading), Toast.LENGTH_SHORT).show()
            rewardedAds.preload()
        }
    }

    val shareStrings = ShareStrings(
        subject = stringResource(R.string.share_subject),
        chooserTitle = stringResource(R.string.share_chooser_title),
        appNotInstalled = stringResource(R.string.share_app_not_installed),
        instagramPaste = stringResource(R.string.share_instagram_paste),
    )
    val statsBlock = if (shareFromVictory) {
        stringResource(
            R.string.share_message_victory,
            formatTime(gameState.elapsedSeconds),
            shareDifficultyLabel(gameState.difficulty, context.resources),
        )
    } else if (playerStats.gamesWon == 0 && playerStats.gamesPlayed == 0) {
        stringResource(R.string.share_message)
    } else {
        stringResource(
            R.string.share_message_with_stats,
            playerStats.gamesWon,
            playerStats.gamesPlayed,
            playerStats.bestWinStreak,
            playerStats.consecutiveDays,
        )
    }
    val shareMessage = stringResource(
        R.string.share_message_full,
        statsBlock,
        ShareHelper.playStoreUrl(),
    )

    DisposableEffect(Unit) {
        feedback.updateSettings(settings)
        onDispose { feedback.release() }
    }

    LaunchedEffect(settings) {
        feedback.updateSettings(settings)
    }

    LaunchedEffect(pendingReviewAfterWin, showWin, showShare, interstitialActive) {
        if (!pendingReviewAfterWin) return@LaunchedEffect
        if (showWin || showShare || interstitialActive) return@LaunchedEffect
        delay(REVIEW_POST_DIALOG_DELAY_MS)
        if (!pendingReviewAfterWin || showWin || showShare || interstitialActive) {
            return@LaunchedEffect
        }
        if (System.currentTimeMillis() - lastFullscreenAdEndMs < REVIEW_AFTER_AD_COOLDOWN_MS) {
            pendingReviewAfterWin = false
            return@LaunchedEffect
        }
        pendingReviewAfterWin = false
        val act = activity ?: return@LaunchedEffect
        PlayReviewHelper.maybeRequestReview(act, dataStore)
    }

    LaunchedEffect(gameState.status) {
        if (gameState.status != GameStatus.WON || showWin || victoryProcessed) return@LaunchedEffect
        victoryProcessed = true
        feedback.onWin()
        val result = dataStore.recordWin(
            gameState.difficulty,
            gameState.elapsedSeconds,
            gameState.mistakes,
        )
        dataStore.clearSavedGame()
        playerStats = dataStore.loadStats()
        AppAnalytics.logGameWon(
            difficulty = gameState.difficulty,
            elapsedSeconds = gameState.elapsedSeconds,
            mistakes = gameState.mistakes,
        )
        result.newUnlocks.forEach { AppAnalytics.logAchievementUnlock(it) }
        AppAnalytics.syncEngagementUserProperties(
            gamesWon = playerStats.gamesWon,
            gamesPlayed = playerStats.gamesPlayed,
            soundEnabled = settings.soundEnabled,
            dailyReminderEnabled = settings.dailyReminderEnabled,
            lastDifficulty = gameState.difficulty,
        )

        achievementToasts = buildAchievementToastMessages(
            unlocks = result.newUnlocks,
            beatBestTime = result.beatBestTime,
            bestTimeDifficulty = result.bestTimeDifficulty,
            elapsedSeconds = gameState.elapsedSeconds,
            res = context.resources,
        )

        val act = activity
        if (act != null) {
            PlayGamesHelper.submitScore(act, gameState.difficulty, gameState.elapsedSeconds)
            PlayGamesHelper.syncAchievements(act, result.newUnlocks)
        }

        if (dataStore.shouldRequestReviewAfterWin(playerStats)) {
            pendingReviewAfterWin = true
        }

        showWin = true
    }

    LaunchedEffect(gameState) {
        if (gameState.status == GameStatus.PLAYING) {
            dataStore.saveGame(AppDataStore.gameToJson(gameState))
        }
    }

    LaunchedEffect(Unit) {
        val before = playerStats.consecutiveDays
        val updated = dataStore.recordDailyPlay()
        playerStats = updated
        if (updated.consecutiveDays > before && updated.consecutiveDays > 1) {
            Toast.makeText(
                context,
                context.getString(R.string.toast_streak_days, updated.consecutiveDays),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    LaunchedEffect(openFromDailyReminder) {
        if (openFromDailyReminder && !dailyChallengeInfo.completed) {
            showStats = true
        }
    }

    LaunchedEffect(Unit) {
        AppAnalytics.logGameStart(gameState.difficulty, "session_start")
        AppAnalytics.syncEngagementUserProperties(
            gamesWon = playerStats.gamesWon,
            gamesPlayed = playerStats.gamesPlayed,
            soundEnabled = settings.soundEnabled,
            dailyReminderEnabled = settings.dailyReminderEnabled,
            lastDifficulty = gameState.difficulty,
        )
        MobileAdsInitializer.whenReady {
            rewardedAds.preload()
        }
    }

    LaunchedEffect(freeHintsRemaining, earnedHints) {
        if (freeHintsRemaining == 0 && earnedHints == 0) {
            MobileAdsInitializer.whenReady { rewardedAds.preload() }
        }
    }

    LaunchedEffect(Unit) {
        val act = activity
        if (act != null) {
            interstitialAdManager?.markGameSessionStart()
        }
    }

    LaunchedEffect(appInForeground, interstitialActive) {
        while (true) {
            delay(1000)
            if (!appInForeground || interstitialActive) continue
            nowMs = System.currentTimeMillis()
            gameState = gameState.tickSecond()
        }
    }

    fun applyNewGame(difficulty: SudokuDifficulty = gameState.difficulty) {
        gameState = SudokuGameState.newGame(SudokuEngine.generate(difficulty))
        showWin = false
        victoryProcessed = false
        dataStore.clearSavedGame()
        AppAnalytics.logButtonClick("new_game_${difficulty.name.lowercase()}")
    }

    fun showInterstitialThenApplyNewGame(difficulty: SudokuDifficulty = gameState.difficulty) {
        val host = activity
        if (host == null) {
            applyNewGame(difficulty)
            return
        }
        MobileAdsInitializer.whenReady {
            val shown = interstitialAdManager?.showAfterVictory(
                activity = host,
                onAdStarted = { beginFullscreenAd() },
                onFinished = {
                    endFullscreenAd()
                    applyNewGame(difficulty)
                },
            ) ?: false
            if (!shown) applyNewGame(difficulty)
        }
    }

    fun startNewGameFromBoard(difficulty: SudokuDifficulty = gameState.difficulty) {
        val boardNewGameCount = dataStore.incrementBoardNewGameCount()
        if (boardNewGameCount % BOARD_NEW_GAMES_PER_INTERSTITIAL == 0) {
            showInterstitialThenApplyNewGame(difficulty)
        } else {
            applyNewGame(difficulty)
        }
    }

    fun startNewGameFromVictoryDialog() {
        showWin = false
        showInterstitialThenApplyNewGame()
    }

    fun toggleAudio() {
        val updated = settings.copy(soundEnabled = !audioEnabled)
        onSettingsChange(updated)
        AppAnalytics.logSoundToggled(updated.soundEnabled)
    }

    fun onBoardDigit(digit: Int) {
        val before = gameState.userValues[gameState.selectedIndex]
        gameState = gameState.enterDigit(digit)
        if (gameState.selectedIndex in 0..80 && !gameState.fixed[gameState.selectedIndex]) {
            val after = gameState.userValues[gameState.selectedIndex]
            if (after != before && after != 0) {
                feedback.onCellFilled(after == gameState.solution[gameState.selectedIndex])
            }
        }
    }

    fun onDifficultySelect(diff: SudokuDifficulty) {
        if (!playerStats.isDifficultyUnlocked(diff)) {
            val progress = playerStats.winsTowardUnlock(diff) ?: return
            val messageRes = when (diff) {
                SudokuDifficulty.MEDIUM -> R.string.difficulty_lock_medium
                SudokuDifficulty.HARD -> R.string.difficulty_lock_hard
                else -> return
            }
            Toast.makeText(
                context,
                context.getString(messageRes, progress.first, progress.second),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        if (diff != gameState.difficulty) {
            startNewGameFromBoard(diff)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SudokuBackgroundStart, SudokuBackgroundMid, SudokuBackgroundEnd),
                ),
            ),
    ) {
        val layout = rememberSudokuLayoutInfo(largeCells = settings.largeCells)
        val screenHeight = maxHeight
        val useSpaceBetween = layout.isTallScreen

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .then(
                    if (useSpaceBetween) {
                        Modifier.padding(vertical = layout.screenVerticalPadding)
                    } else {
                        Modifier
                    },
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (useSpaceBetween) {
                Arrangement.SpaceBetween
            } else {
                Arrangement.Top
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
            ) {
                SudokuGameHeader(
                    audioEnabled = audioEnabled,
                    layout = layout,
                    showLeaderboards = playGamesLeaderboardsEnabled && activity != null,
                    onOpenShare = {
                        showShare = true
                        AppAnalytics.logButtonClick("open_share")
                    },
                    onOpenStats = {
                        showStats = true
                        AppAnalytics.logMenuOpen("stats")
                    },
                    onOpenSettings = {
                        AppAnalytics.logMenuOpen("settings")
                        onOpenSettings()
                    },
                    onOpenLeaderboards = {
                        AppAnalytics.logMenuOpen("leaderboards")
                        openPlayGamesLeaderboards(gameState.difficulty)
                    },
                    onShowInstructions = {
                        showInstructions = true
                        AppAnalytics.logMenuOpen("instructions")
                    },
                    onToggleAudio = ::toggleAudio,
                    headerIconSize = layout.headerIconSize,
                    titleFontSize = layout.titleFontSize,
                    subtitleFontSize = layout.subtitleFontSize,
                    headerIconContentSize = layout.headerIconContentSize,
                    verticalPadding = when {
                        layout.isTallScreen -> 4.dp
                        layout.isShortScreen -> 2.dp
                        else -> 4.dp
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                DailyChallengeCard(
                    info = dailyChallengeInfo,
                    layout = layout,
                    consecutiveDays = playerStats.consecutiveDays,
                    onTap = {
                        showStats = true
                        AppAnalytics.logButtonClick("open_daily_challenge")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = layout.horizontalPadding),
                )

                SudokuStatusCard(
                    elapsedSeconds = gameState.elapsedSeconds,
                    mistakes = gameState.mistakes,
                    notesMode = gameState.notesMode,
                    freeHints = freeHintsMax,
                    freeHintsRemaining = freeHintsRemaining,
                    earnedHintsAvailable = earnedHints,
                    showHintVideo = freeHintsRemaining == 0 && earnedHints == 0,
                    onReset = {
                        startNewGameFromBoard()
                        AppAnalytics.logButtonClick("reset_game")
                    },
                    onToggleNotes = {
                        gameState = gameState.toggleNotesMode()
                    },
                    onHint = { onHintClick() },
                    isCompact = layout.isCompact,
                    scrollStatusBar = layout.scrollStatusBar,
                    chipHeight = layout.statusChipHeight,
                    captionFontSize = layout.statusCaptionFontSize,
                    valueFontSize = layout.statusValueFontSize,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = layout.horizontalPadding),
                )

                DifficultyRow(
                    selected = gameState.difficulty,
                    playerStats = playerStats,
                    layout = layout,
                    horizontalPadding = layout.horizontalPadding,
                    onSelect = ::onDifficultySelect,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!useSpaceBetween) {
                            Modifier.weight(1f)
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = layout.horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = when {
                    useSpaceBetween || layout.isShortScreen -> Arrangement.Top
                    else -> Arrangement.Center
                },
            ) {
                if (useSpaceBetween) {
                    SudokuBoard(
                        state = gameState,
                        settings = settings,
                        onCellClick = { index ->
                            feedback.onTap()
                            gameState = gameState.withSelection(index)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = layout.distributedBoardMaxHeight(screenHeight)),
                    )
                } else {
                    SudokuBoard(
                        state = gameState,
                        settings = settings,
                        onCellClick = { index ->
                            feedback.onTap()
                            gameState = gameState.withSelection(index)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(layout.sectionSpacing))
                    NumberPad(
                        layout = layout,
                        gameState = gameState,
                        onDigit = ::onBoardDigit,
                        onErase = { gameState = gameState.eraseCell() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (useSpaceBetween) {
                NumberPad(
                    layout = layout,
                    gameState = gameState,
                    onDigit = ::onBoardDigit,
                    onErase = { gameState = gameState.eraseCell() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = layout.horizontalPadding),
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                BannerAdView(
                    modifier = Modifier
                        .width(layout.bannerWidth)
                        .navigationBarsPadding(),
                )
            }
        }

        AchievementToastHost(
            messages = achievementToasts,
            onDismissed = { achievementToasts = emptyList() },
        )

        if (showShare) {
            ShareSocialSheet(
                visible = showShare,
                onDismiss = {
                    showShare = false
                    shareFromVictory = false
                },
                onShare = { platform ->
                    AppAnalytics.logShare(platform)
                    AppAnalytics.logPostGameShare(shareFromVictory)
                    ShareHelper.share(context, platform, shareMessage, shareStrings)
                },
                headerIconSize = layout.headerIconSize,
                screenHorizontalPadding = layout.horizontalPadding,
            )
        }
    }

    if (showInstructions) {
        LaunchedEffect(Unit) { AppAnalytics.logScreenView("instructions") }
        InstructionsDialog(onDismiss = { showInstructions = false })
    }

    if (showStats) {
        LaunchedEffect(Unit) { AppAnalytics.logScreenView("stats") }
        StatsDialog(
            stats = playerStats,
            dailyInfo = dailyChallengeInfo,
            onDismiss = { showStats = false },
            showPlayGamesActions = playGamesLeaderboardsEnabled && activity != null,
            onOpenLeaderboard = activity?.let {
                {
                    showStats = false
                    AppAnalytics.logMenuOpen("leaderboards_stats")
                    openPlayGamesLeaderboards(gameState.difficulty)
                }
            },
            onOpenAchievements = activity?.let { act ->
                {
                    showStats = false
                    AppAnalytics.logMenuOpen("achievements_stats")
                    PlayGamesHelper.showAchievements(act)
                }
            },
        )
    }

    if (showWin) {
        WinDialog(
            elapsedSeconds = gameState.elapsedSeconds,
            mistakes = gameState.mistakes,
            showLeaderboards = playGamesLeaderboardsEnabled && activity != null,
            onOpenLeaderboards = activity?.let {
                {
                    showWin = false
                    AppAnalytics.logMenuOpen("leaderboards_win")
                    openPlayGamesLeaderboards(gameState.difficulty)
                }
            },
            onDismiss = { showWin = false },
            onShare = {
                showWin = false
                shareFromVictory = true
                showShare = true
                AppAnalytics.logButtonClick("win_share")
            },
            onNewGame = {
                AppAnalytics.logButtonClick("win_new_game")
                startNewGameFromVictoryDialog()
            },
        )
    }
}

private const val REVIEW_POST_DIALOG_DELAY_MS = 800L
private const val REVIEW_AFTER_AD_COOLDOWN_MS = 120_000L
private const val BOARD_NEW_GAMES_PER_INTERSTITIAL = 3

private fun loadInitialGameState(
    dataStore: AppDataStore,
    stats: PlayerStats,
): SudokuGameState {
    val saved = dataStore.loadSavedGame()?.let { AppDataStore.gameFromJson(it) }
    val candidate = saved ?: SudokuGameState.newGame(
        SudokuEngine.generate(SudokuDifficulty.EASY),
    )
    return if (stats.isDifficultyUnlocked(candidate.difficulty)) {
        candidate
    } else {
        SudokuGameState.newGame(
            SudokuEngine.generate(stats.highestUnlockedDifficulty()),
        )
    }
}

@Composable
private fun DifficultyRow(
    selected: SudokuDifficulty,
    playerStats: PlayerStats,
    layout: SudokuLayoutInfo,
    horizontalPadding: Dp = layout.horizontalPadding,
    onSelect: (SudokuDifficulty) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        SudokuDifficulty.entries.forEach { diff ->
            val label = when (diff) {
                SudokuDifficulty.EASY -> stringResource(R.string.difficulty_easy)
                SudokuDifficulty.MEDIUM -> stringResource(R.string.difficulty_medium)
                SudokuDifficulty.HARD -> stringResource(R.string.difficulty_hard)
            }
            val isUnlocked = playerStats.isDifficultyUnlocked(diff)
            val isSelected = selected == diff && isUnlocked
            val progress = playerStats.winsTowardUnlock(diff)
            val chipAccent = when (diff) {
                SudokuDifficulty.EASY -> SudokuPrimary
                SudokuDifficulty.MEDIUM -> SudokuAccent
                SudokuDifficulty.HARD -> SudokuNeonPink
            }
            val chipModifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isSelected) {
                        Modifier.shadow(
                            4.dp,
                            RoundedCornerShape(16.dp),
                            ambientColor = chipAccent,
                            spotColor = chipAccent,
                        )
                    } else {
                        Modifier
                    },
                )
                .background(
                    when {
                        isSelected -> Brush.horizontalGradient(
                            listOf(chipAccent, chipAccent.copy(alpha = 0.7f)),
                        )
                        !isUnlocked -> Brush.verticalGradient(
                            listOf(Color(0xFF1A1030), Color(0xFF0E0820)),
                        )
                        else -> Brush.verticalGradient(
                            listOf(SudokuChromeBg, Color(0xFF120828)),
                        )
                    },
                )
                .border(
                    1.dp,
                    when {
                        isSelected -> chipAccent
                        !isUnlocked -> Color.White.copy(alpha = 0.2f)
                        else -> chipAccent.copy(alpha = 0.45f)
                    },
                    RoundedCornerShape(16.dp),
                )
                .clickable { onSelect(diff) }
                .padding(
                    horizontal = layout.chipHorizontalPadding,
                    vertical = layout.chipVerticalPadding,
                )

            val chipContent: @Composable () -> Unit = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    if (!isUnlocked) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = chipAccent.copy(alpha = 0.75f),
                            modifier = Modifier.size(11.dp),
                        )
                    }
                    Text(
                        text = label,
                        style = TextStyle(
                            fontSize = layout.chipFontSize,
                            fontWeight = FontWeight.Bold,
                            lineHeight = layout.chipFontSize,
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            color = when {
                                isSelected -> Color(0xFF001A22)
                                !isUnlocked -> Color.White.copy(alpha = 0.45f)
                                else -> Color.White
                            },
                        ),
                    )
                    if (!isUnlocked && progress != null) {
                        Text(
                            text = "${progress.first}/${progress.second}",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = layout.chipFontSize,
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                color = chipAccent,
                            ),
                        )
                    }
                }
            }

            Box(modifier = chipModifier, contentAlignment = Alignment.Center) {
                chipContent()
            }
        }
    }
}

@Composable
private fun NumberPad(
    layout: SudokuLayoutInfo,
    gameState: SudokuGameState,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(layout.numpadHeight),
        horizontalArrangement = Arrangement.spacedBy(layout.keySpacing),
    ) {
        for (n in 1..9) {
            NumberKey(
                digit = n,
                enabled = gameState.canEnterDigit(n),
                onDigit = onDigit,
                fontSize = layout.keyFontSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
        EraseKey(
            onErase = onErase,
            fontSize = layout.keyFontSize,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun NumberKey(
    digit: Int,
    enabled: Boolean,
    onDigit: (Int) -> Unit,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .then(
                if (enabled) {
                    Modifier.shadow(
                        3.dp,
                        shape,
                        ambientColor = SudokuPrimary,
                        spotColor = SudokuPrimary,
                    )
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .background(
                if (enabled) {
                    Brush.verticalGradient(listOf(Color(0xFF00E5FF), Color(0xFF0097A7)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF2A3548), Color(0xFF1E2838)))
                },
            )
            .border(
                1.dp,
                Color.White.copy(alpha = if (enabled) 0.35f else 0.12f),
                shape,
            )
            .clickable(enabled = enabled) { onDigit(digit) },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = if (enabled) Color(0xFF001A22) else Color.White.copy(alpha = 0.35f),
        )
    }
}

@Composable
private fun EraseKey(
    onErase: () -> Unit,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.game_erase)
    Box(
        modifier = modifier
            .semantics { contentDescription = label }
            .shadow(3.dp, RoundedCornerShape(8.dp), ambientColor = SudokuNeonPink)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(listOf(SudokuNeonPink, Color(0xFFC51162))),
            )
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .clickable(onClick = onErase),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "⌫",
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = Color.White,
        )
    }
}
