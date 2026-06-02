package com.template.android.ui.sudoku

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import android.widget.Toast
import com.template.android.data.PlayerStats
import com.template.android.ui.sudoku.SudokuDifficultyUnlock.highestUnlockedDifficulty
import com.template.android.ui.sudoku.SudokuDifficultyUnlock.isDifficultyUnlocked
import com.template.android.ui.sudoku.SudokuDifficultyUnlock.winsTowardUnlock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.android.R
import com.template.android.ads.BannerAdView
import com.template.android.ads.InterstitialAdManager
import com.template.android.ads.MobileAdsInitializer
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AppDataStore
import com.template.android.data.AppSettings
import com.template.android.data.HintState
import com.template.android.engagement.PlayReviewHelper
import com.template.android.ui.layout.SudokuLayoutInfo
import com.template.android.ui.layout.boardSizeFor
import com.template.android.ui.layout.rememberSudokuLayoutInfo
import com.template.android.ui.theme.SudokuAccent
import com.template.android.ui.theme.SudokuBackgroundEnd
import com.template.android.ui.theme.SudokuBackgroundMid
import com.template.android.ui.theme.SudokuBackgroundStart
import com.template.android.ui.theme.SudokuChromeBg
import com.template.android.ui.theme.SudokuNeonPink
import com.template.android.ui.theme.SudokuPrimary
import kotlinx.coroutines.delay

@Composable
fun SudokuScreen(
    dataStore: AppDataStore,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onOpenSettings: () -> Unit,
    interstitialAdManager: InterstitialAdManager?,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val feedback = remember { GameFeedback(context) }

    var playerStats by remember { mutableStateOf(dataStore.loadStats()) }
    var gameState by remember {
        mutableStateOf(loadInitialGameState(dataStore, playerStats))
    }
    var showStats by remember { mutableStateOf(false) }
    var showWin by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var hintState by remember { mutableStateOf(dataStore.loadHintState()) }
    var earnedHints by remember { mutableStateOf(0) }
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var interstitialActive by remember { mutableStateOf(false) }

    val audioEnabled = settings.soundEnabled
    val freeHintsRemaining = hintState.freeHintsRemaining
    val freeHintsMax = 2
    val freeHintsUsed = (freeHintsMax - freeHintsRemaining).coerceIn(0, freeHintsMax)
    val hintRefillIntervalMs = 10 * 60 * 1000L // 10 minutos
    val nextHintInSeconds: Int? = run {
        // Mostrar contador solo cuando ya se consumieron todas las pistas gratis
        if (freeHintsRemaining > 0) return@run null
        val base = if (hintState.lastRefillAtMs > 0L) hintState.lastRefillAtMs else nowMs
        val remainingMs = (hintRefillIntervalMs - (nowMs - base)).coerceAtLeast(0L)
        (remainingMs / 1000L).toInt()
    }

    fun applyHint(consumesFree: Boolean) {
        val (next, applied) = gameState.applyHint()
        if (!applied) return
        gameState = next
        if (consumesFree) {
            val remainingAfter = (hintState.freeHintsRemaining - 1).coerceAtLeast(0)
            val updated = hintState.copy(
                freeHintsRemaining = remainingAfter,
                // El timer empieza cuando se consumen TODAS las pistas (llegas a 0).
                lastRefillAtMs = when {
                    remainingAfter > 0 -> hintState.lastRefillAtMs
                    hintState.lastRefillAtMs > 0L -> hintState.lastRefillAtMs
                    else -> System.currentTimeMillis()
                },
            )
            hintState = updated
            dataStore.saveHintState(updated)
        } else {
            earnedHints = (earnedHints - 1).coerceAtLeast(0)
        }
        // Pista siempre es correcta, así que damos feedback “correcto”
        feedback.onCellFilled(true)
    }

    fun onHintClick() {
        // 2 gratis: muestra contador 0/2, 1/2, 2/2.
        if (freeHintsRemaining > 0) {
            AppAnalytics.logButtonClick("hint_free")
            applyHint(consumesFree = true)
            return
        }

        // Si ya hay pistas ganadas por anuncio, úsala sin pedir otro anuncio.
        if (earnedHints > 0) {
            AppAnalytics.logButtonClick("hint_earned")
            applyHint(consumesFree = false)
            return
        }

        // Después de las 2 gratis: botón con icono de video para monetizar.
        val act = activity ?: return
        val manager = interstitialAdManager ?: return
        AppAnalytics.logButtonClick("hint_video_request")
        MobileAdsInitializer.whenReady {
            manager.showIfReady(
                act,
                shouldShow = true,
                ignoreCooldown = true,
                onAdStarted = { interstitialActive = true },
                onAdShown = {
                    // Tras el anuncio: 1 pista disponible; el jugador la usa con otro toque.
                    earnedHints = 1
                    AppAnalytics.logButtonClick("hint_video_reward")
                },
                onFinished = { interstitialActive = false },
            )
        }
    }

    val shareStrings = ShareStrings(
        subject = stringResource(R.string.share_subject),
        chooserTitle = stringResource(R.string.share_chooser_title),
        appNotInstalled = stringResource(R.string.share_app_not_installed),
        instagramPaste = stringResource(R.string.share_instagram_paste),
    )
    val statsBlock = if (playerStats.gamesWon == 0 && playerStats.gamesPlayed == 0) {
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

    LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.WON && !showWin) {
            showWin = true
            feedback.onWin()
            dataStore.recordWin(gameState.difficulty, gameState.elapsedSeconds)
            dataStore.clearSavedGame()
            playerStats = dataStore.loadStats()
            AppAnalytics.logButtonClick("puzzle_complete_${gameState.difficulty.name.lowercase()}")

            // Review: pedir una vez al llegar a 3 victorias totales.
            val act = activity
            if (act != null && playerStats.gamesWon >= 3 && !dataStore.isReviewRequested()) {
                dataStore.setReviewRequested()
                PlayReviewHelper.requestReview(act)
            }
        }
    }

    LaunchedEffect(gameState) {
        if (gameState.status == GameStatus.PLAYING) {
            dataStore.saveGame(AppDataStore.gameToJson(gameState))
        }
    }

    LaunchedEffect(Unit) {
        playerStats = dataStore.recordDailyPlay()
    }

    LaunchedEffect(Unit) {
        val act = activity
        if (act != null) {
            interstitialAdManager?.markGameSessionStart()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (!interstitialActive) {
                gameState = gameState.tickSecond()
            }
            nowMs = System.currentTimeMillis()
        }
    }

    // Intersticial por tiempo: intenta mostrarlo cada ~5s, pero el manager aplica cooldown de 90s.
    LaunchedEffect(Unit) {
        while (true) {
            delay(5_000)
            if (gameState.status != GameStatus.PLAYING) continue
            val act = activity ?: continue
            interstitialAdManager?.maybeShowTimed(
                act,
                onAdStarted = { interstitialActive = true },
                onFinished = { interstitialActive = false },
            )
        }
    }

    LaunchedEffect(nowMs, hintState.freeHintsRemaining, hintState.lastRefillAtMs) {
        if (hintState.freeHintsRemaining >= freeHintsMax) return@LaunchedEffect
        val base = if (hintState.lastRefillAtMs > 0L) hintState.lastRefillAtMs else nowMs
        if (nowMs - base < hintRefillIntervalMs) return@LaunchedEffect
        val updated = HintState(
            freeHintsRemaining = (hintState.freeHintsRemaining + 1).coerceAtMost(freeHintsMax),
            lastRefillAtMs = base + hintRefillIntervalMs,
        )
        hintState = updated
        dataStore.saveHintState(updated)
    }

    fun startNewGame(difficulty: SudokuDifficulty = gameState.difficulty) {
        gameState = SudokuGameState.newGame(SudokuEngine.generate(difficulty))
        showWin = false
        earnedHints = 0
        dataStore.clearSavedGame()
        AppAnalytics.logButtonClick("new_game_${difficulty.name.lowercase()}")
    }

    fun toggleAudio() {
        val updated = settings.copy(soundEnabled = !audioEnabled)
        onSettingsChange(updated)
        AppAnalytics.logButtonClick(if (updated.soundEnabled) "audio_on" else "audio_off")
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SudokuGameHeader(
                audioEnabled = audioEnabled,
                consecutiveDays = playerStats.consecutiveDays,
                layout = layout,
                onOpenShare = {
                    showShare = true
                    AppAnalytics.logButtonClick("open_share")
                },
                onOpenStats = {
                    showStats = true
                    AppAnalytics.logButtonClick("open_stats")
                },
                onOpenSettings = onOpenSettings,
                onToggleAudio = ::toggleAudio,
                headerIconSize = layout.headerIconSize,
                headerSideWidth = layout.headerSideWidth,
                titleFontSize = layout.titleFontSize,
                subtitleFontSize = layout.subtitleFontSize,
                headerIconContentSize = layout.headerIconContentSize,
                verticalPadding = if (layout.isShortScreen) 4.dp else 6.dp,
                modifier = Modifier.fillMaxWidth(),
            )

            SudokuStatusCard(
                elapsedSeconds = gameState.elapsedSeconds,
                mistakes = gameState.mistakes,
                notesMode = gameState.notesMode,
                hintsUsed = freeHintsUsed,
                freeHints = freeHintsMax,
                earnedHintsAvailable = earnedHints,
                showHintVideo = freeHintsRemaining == 0 && earnedHints == 0,
                onReset = {
                    startNewGame()
                    AppAnalytics.logButtonClick("reset_game")
                },
                onToggleNotes = {
                    gameState = gameState.toggleNotesMode()
                },
                onHint = ::onHintClick,
                isCompact = layout.isCompact,
                chipHeight = layout.statusChipHeight,
                captionFontSize = layout.statusCaptionFontSize,
                valueFontSize = layout.statusValueFontSize,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = layout.sectionSpacing),
            )

            DifficultyRow(
                selected = gameState.difficulty,
                playerStats = playerStats,
                layout = layout,
                nextHintSeconds = nextHintInSeconds,
                onSelect = { diff ->
                    if (!playerStats.isDifficultyUnlocked(diff)) {
                        val progress = playerStats.winsTowardUnlock(diff) ?: return@DifficultyRow
                        val messageRes = when (diff) {
                            SudokuDifficulty.MEDIUM -> R.string.difficulty_lock_medium
                            SudokuDifficulty.HARD -> R.string.difficulty_lock_hard
                            else -> return@DifficultyRow
                        }
                        Toast.makeText(
                            context,
                            context.getString(messageRes, progress.first, progress.second),
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@DifficultyRow
                    }
                    if (diff != gameState.difficulty) {
                        startNewGame(diff)
                    }
                },
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val boardSize = layout.boardSizeFor(maxHeight)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (layout.isShortScreen) {
                        Arrangement.Top
                    } else {
                        Arrangement.Center
                    },
                ) {
                    SudokuBoard(
                        state = gameState,
                        settings = settings,
                        onCellClick = { index ->
                            feedback.onTap()
                            gameState = gameState.withSelection(index)
                        },
                        modifier = Modifier.size(boardSize),
                    )
                    Spacer(modifier = Modifier.height(layout.sectionSpacing))
                    NumberPad(
                        layout = layout,
                        gameState = gameState,
                        onDigit = { digit ->
                            val before = gameState.userValues[gameState.selectedIndex]
                            gameState = gameState.enterDigit(digit)
                            if (gameState.selectedIndex in 0..80 &&
                                !gameState.fixed[gameState.selectedIndex]
                            ) {
                                val after = gameState.userValues[gameState.selectedIndex]
                                if (after != before && after != 0) {
                                    feedback.onCellFilled(
                                        after == gameState.solution[gameState.selectedIndex],
                                    )
                                }
                            }
                        },
                        onErase = {
                            gameState = gameState.eraseCell()
                        },
                        modifier = Modifier
                            .width(boardSize)
                            .heightIn(min = layout.numpadHeight),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = layout.sectionSpacing),
                contentAlignment = Alignment.Center,
            ) {
                BannerAdView(
                    modifier = Modifier.width(layout.bannerWidth),
                )
            }
        }

        if (showShare) {
            ShareSocialSheet(
                visible = showShare,
                onDismiss = { showShare = false },
                onShare = { platform ->
                    AppAnalytics.logShare(platform)
                    ShareHelper.share(context, platform, shareMessage, shareStrings)
                },
                headerIconSize = layout.headerIconSize,
                screenHorizontalPadding = layout.horizontalPadding,
            )
        }
    }

    if (showStats) {
        StatsDialog(stats = playerStats, onDismiss = { showStats = false })
    }

    if (showWin) {
        WinDialog(
            elapsedSeconds = gameState.elapsedSeconds,
            mistakes = gameState.mistakes,
            onDismiss = { showWin = false },
            onShare = {
                showWin = false
                showShare = true
                AppAnalytics.logButtonClick("win_share")
            },
            onNewGame = {
                showWin = false
                startNewGame()
            },
        )
    }
}

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
    nextHintSeconds: Int?,
    onSelect: (SudokuDifficulty) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = layout.horizontalPadding)
            .padding(bottom = layout.sectionSpacing),
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

            if (diff == SudokuDifficulty.HARD && nextHintSeconds != null) {
                val mm = nextHintSeconds / 60
                val ss = nextHintSeconds % 60
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(modifier = chipModifier, contentAlignment = Alignment.Center) {
                        chipContent()
                    }
                    Text(
                        text = "${stringResource(R.string.hints_next)} ${"%d:%02d".format(mm, ss)}",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = layout.chipFontSize,
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            color = SudokuPrimary.copy(alpha = 0.9f),
                        ),
                        maxLines = 1,
                    )
                }
            } else {
                Box(modifier = chipModifier, contentAlignment = Alignment.Center) {
                    chipContent()
                }
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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(layout.keySpacing),
    ) {
        for (n in 1..9) {
            NumberKey(
                digit = n,
                enabled = gameState.canEnterDigit(n),
                onDigit = onDigit,
                verticalPadding = layout.keyVerticalPadding,
                fontSize = layout.keyFontSize,
                modifier = Modifier.weight(1f),
            )
        }
        EraseKey(
            onErase = onErase,
            verticalPadding = layout.keyVerticalPadding,
            fontSize = layout.keyFontSize,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NumberKey(
    digit: Int,
    enabled: Boolean,
    onDigit: (Int) -> Unit,
    verticalPadding: androidx.compose.ui.unit.Dp,
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
            .clickable(enabled = enabled) { onDigit(digit) }
            .padding(vertical = verticalPadding),
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
    verticalPadding: androidx.compose.ui.unit.Dp,
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
            .clickable(onClick = onErase)
            .padding(vertical = verticalPadding),
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
