package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.StartIoManager
import com.example.ui.EchoGameScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EchoGameViewModel

class MainActivity : ComponentActivity() {
  private var activeGameViewModel: EchoGameViewModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Start.io Ads SDK with App ID: 208838202 (Test Ads Mode)
    val echoPrefs = com.example.data.EchoPreferences(this)
    StartIoManager.initialize(this, testMode = echoPrefs.isTestAdsEnabled)
    com.example.audio.HarmonicAudioEngine.init(this)

    setContent {
      MyApplicationTheme(darkTheme = false) {
        val gameViewModel: EchoGameViewModel = viewModel()
        activeGameViewModel = gameViewModel
        EchoGameScreen(
          viewModel = gameViewModel,
          onShowRewardedAd = { type ->
            gameViewModel.setAdLoading(true, type)
            StartIoManager.showRewardedVideoAd(
              activity = this,
              onRewardEarned = {
                gameViewModel.grantRewardedReward(type)
              },
              onAdDisplayed = {
                gameViewModel.setAdLoading(false)
              },
              onAdClosed = {
                gameViewModel.setAdLoading(false)
              },
              onAdUnavailable = { reason ->
                gameViewModel.setAdLoading(false)
                gameViewModel.onAdUnavailable(type, reason)
              }
            )
          },
          onShowInterstitialAd = { onAdFinished ->
            StartIoManager.showInterstitialAd(
              activity = this,
              onAdDisplayed = { },
              onAdClosed = { onAdFinished() }
            )
          }
        )
      }
    }
  }

  override fun onResume() {
    super.onResume()
    com.example.audio.HarmonicAudioEngine.resumeBgm()
    StartIoManager.preloadAds(this)
    activeGameViewModel?.checkAdBlocker()
  }

  override fun onPause() {
    super.onPause()
    com.example.audio.HarmonicAudioEngine.pauseBgm()
  }

  @Deprecated("Deprecated in Java")
  override fun onBackPressed() {
    StartIoManager.onBackPressed(this)
    super.onBackPressed()
  }
}
