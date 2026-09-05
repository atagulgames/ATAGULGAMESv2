package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameStatus
import com.example.model.ScreenState
import com.example.ui.components.EchoBottomHUD
import com.example.ui.components.EchoCanvas
import com.example.ui.components.EchoToastBanner
import com.example.ui.components.EchoTopHUD
import com.example.ui.dialogs.AdBlockerWarningOverlay
import com.example.ui.dialogs.AdLoadingDialog
import com.example.ui.dialogs.DeadlockDialog
import com.example.ui.dialogs.LevelSelectDialog
import com.example.ui.dialogs.RewardClaimedDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.ShopDialog
import com.example.ui.dialogs.SkinsDialog
import com.example.ui.dialogs.StartIoRewardFallbackDialog
import com.example.ui.dialogs.VictoryDialog
import com.example.ui.intro.IntroLandscapeScreen
import com.example.ui.menu.EchoMainMenu
import com.example.viewmodel.EchoGameViewModel

@Composable
fun EchoGameScreen(
    viewModel: EchoGameViewModel = viewModel(),
    onShowRewardedAd: (String) -> Unit = {},
    onShowInterstitialAd: (() -> Unit) -> Unit = { it() },
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle system back button to return to Main Menu from game
    BackHandler(enabled = state.screenState != ScreenState.MAIN_MENU && state.screenState != ScreenState.INTRO) {
        viewModel.returnToMainMenu()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("echo_game_scaffold"),
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.screenState) {
                ScreenState.INTRO -> {
                    // Fluid, modern intro with official logo and auto/tap transition
                    IntroLandscapeScreen(
                        onIntroFinished = { viewModel.finishIntro() }
                    )
                }

                ScreenState.MAIN_MENU -> {
                    EchoMainMenu(
                        state = state,
                        onPlay = { viewModel.startPlayingLevel() },
                        onDailyChallenge = { viewModel.startDailyChallenge() },
                        onOpenLevelSelect = { viewModel.setLevelSelectVisible(true) },
                        onOpenShop = { viewModel.setShopVisible(true) },
                        onOpenSkins = { viewModel.setSkinsVisible(true) },
                        onOpenSettings = { viewModel.setSettingsVisible(true) },
                        onWatchRewardedAd = { onShowRewardedAd("REWARD_DAILY") }
                    )
                }

                ScreenState.PLAYING_LEVEL, ScreenState.DAILY_CHALLENGE -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top HUD
                        EchoTopHUD(
                            state = state,
                            onBackToMenu = { viewModel.returnToMainMenu() },
                            onOpenLevelSelect = { viewModel.setLevelSelectVisible(true) },
                            onOpenShop = { viewModel.setShopVisible(true) }
                        )

                        // Interactive Canvas with mathematical collision engine
                        EchoCanvas(
                            state = state,
                            onPointerDown = viewModel::onPointerDown,
                            onPointerMove = viewModel::onPointerMove,
                            onPointerUp = viewModel::onPointerUp,
                            modifier = Modifier.weight(1f)
                        )

                        // Bottom Action Bar: Reset, Clear Echoes, Power-ups, Hint
                        EchoBottomHUD(
                            state = state,
                            onReset = { viewModel.restartLevel(clearEchoes = false) },
                            onClearEchoes = { viewModel.clearAllEchoes() },
                            onUseBreaker = { viewModel.useEchoBreaker() },
                            onActivateShrinker = { viewModel.activateEchoShrinker() },
                            onHint = { viewModel.useHint() }
                        )
                    }
                }
            }

            // Toast feedback banner (top-center)
            EchoToastBanner(
                message = state.toastMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            )

            // 1. Victory Dialog
            if (state.gameStatus == GameStatus.VICTORY) {
                VictoryDialog(
                    levelId = state.level.levelId,
                    echoCount = state.echoCountForLevel,
                    parEchoes = state.level.parEchoes,
                    onNextLevel = {
                        onShowInterstitialAd {
                            viewModel.nextLevel()
                        }
                    },
                    onReplay = {
                        viewModel.restartLevel(clearEchoes = true)
                    }
                )
            }

            // 2. Deadlock / Game Over Dialog
            if (state.gameStatus == GameStatus.DEADLOCK) {
                DeadlockDialog(
                    echoCount = state.echoCountForLevel,
                    onClearEchoes = { viewModel.clearAllEchoes() },
                    onClearWithAd = { onShowRewardedAd("CLEAR_ECHOES") },
                    onRestartLevel = { viewModel.restartLevel(clearEchoes = true) }
                )
            }

            // 3. Shop Dialog (IAP Simulation & Start.io Video Rewards)
            if (state.isShopDialogVisible) {
                ShopDialog(
                    currentTokens = state.tokens,
                    currentBreakers = state.echoBreakers,
                    onPurchaseTokens = { count -> viewModel.purchaseTokens(count) },
                    onPurchaseBreakers = { count -> viewModel.purchaseBreakers(count) },
                    onWatchRewardedAd = { onShowRewardedAd("HINT_TOKEN") },
                    onDismiss = { viewModel.setShopVisible(false) }
                )
            }

            // 4. Start.io Fallback / Countdown Simulation Dialog
            if (state.isRewardedSimulating) {
                StartIoRewardFallbackDialog(
                    countdownSeconds = state.rewardCountdownSeconds,
                    onDismiss = { viewModel.dismissRewardSimulation() }
                )
            }

            // 5. Level Selector Dialog (100 levels from Room)
            if (state.isLevelSelectVisible) {
                LevelSelectDialog(
                    levels = state.allLevels,
                    currentLevelIndex = state.currentLevelIndex,
                    completedLevels = state.completedLevels,
                    onSelectLevel = { idx ->
                        viewModel.setLevelSelectVisible(false)
                        viewModel.startPlayingLevel(idx)
                    },
                    onDismiss = { viewModel.setLevelSelectVisible(false) }
                )
            }

            // 6. Settings Dialog
            if (state.isSettingsDialogVisible) {
                SettingsDialog(
                    soundEnabled = state.soundEnabled,
                    hapticsEnabled = state.hapticsEnabled,
                    testAdsEnabled = state.testAdsEnabled,
                    currentLanguage = state.language,
                    onSelectLanguage = { viewModel.setLanguage(it) },
                    onToggleSound = { viewModel.toggleSound(it) },
                    onToggleHaptics = { viewModel.toggleHaptics(it) },
                    onToggleTestAds = { viewModel.toggleTestAds(it) },
                    onResetAllProgress = { viewModel.resetAllGameProgress() },
                    onDismiss = { viewModel.setSettingsVisible(false) }
                )
            }

            // 7. Skins / Themes Dialog
            if (state.isSkinsDialogVisible) {
                SkinsDialog(
                    currentStroke = state.strokeTheme,
                    currentEcho = state.echoTheme,
                    onSelectStroke = { viewModel.setStrokeTheme(it) },
                    onSelectEcho = { viewModel.setEchoTheme(it) },
                    onDismiss = { viewModel.setSkinsVisible(false) }
                )
            }

            // 8. Ad Loading Overlay Dialog
            if (state.isAdLoading) {
                AdLoadingDialog(
                    onDismiss = { viewModel.setAdLoading(false) }
                )
            }

            // 9. Reward Claimed Celebration Dialog
            state.rewardClaimedData?.let { rewardInfo ->
                RewardClaimedDialog(
                    title = rewardInfo.title,
                    subtitle = rewardInfo.subtitle,
                    tokensAdded = rewardInfo.tokensAdded,
                    breakersAdded = rewardInfo.breakersAdded,
                    currentTokens = state.tokens,
                    currentBreakers = state.echoBreakers,
                    onDismiss = { viewModel.dismissRewardClaimedDialog() }
                )
            }

            // 10. Ad Blocker Detected Full-Screen Blocking Warning
            if (state.isAdBlockerDetected) {
                AdBlockerWarningOverlay(
                    isChecking = state.isCheckingAdBlocker,
                    onRecheck = { viewModel.checkAdBlocker() },
                    onExitApp = {
                        val activity = context as? Activity
                        activity?.finishAffinity()
                    }
                )
            }
        }
    }
}
