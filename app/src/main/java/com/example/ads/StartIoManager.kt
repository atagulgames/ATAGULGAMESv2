package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener

object StartIoManager {
    private const val TAG = "StartIoManager"
    const val APP_ID = "208838202"

    private var isInitialized = false
    private var isTestMode = false

    private var preloadedRewardedAd: StartAppAd? = null
    private var preloadedInterstitialAd: StartAppAd? = null

    private var isRewardedLoading = false
    private var isInterstitialLoading = false

    /**
     * Initializes Start.io SDK.
     * When testMode is false (default), Start.io serves live production ads.
     */
    fun initialize(activity: Activity, testMode: Boolean = false) {
        isTestMode = testMode
        try {
            StartAppSDK.setTestAdsEnabled(isTestMode)

            if (!isInitialized) {
                // Initialize with App ID 208838202, return ads disabled
                StartAppSDK.init(activity, APP_ID, false)
                StartAppAd.disableSplash()

                // Submit GDPR consent for maximum fill rate on live ad networks
                StartAppSDK.setUserConsent(activity, "pas", System.currentTimeMillis(), true)
                isInitialized = true
                Log.d(TAG, "Start.io SDK initialized with App ID: $APP_ID (Live Mode=${!isTestMode})")
            }

            preloadAds(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Start.io SDK", e)
        }
    }

    fun setTestModeEnabled(enabled: Boolean, context: Context? = null) {
        isTestMode = enabled
        try {
            StartAppSDK.setTestAdsEnabled(enabled)
            Log.d(TAG, "Start.io test mode updated: $enabled")
            preloadedRewardedAd = null
            preloadedInterstitialAd = null
            if (context is Activity) {
                preloadAds(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating test mode", e)
        }
    }

    fun isTestModeEnabled(): Boolean = isTestMode

    /**
     * Preloads both pure Rewarded Video and Automatic Fullscreen/Interstitial ads
     * so an ad is ready when the user requests it.
     */
    fun preloadAds(activity: Activity) {
        preloadRewardedVideo(activity)
        preloadInterstitial(activity)
    }

    fun preloadRewardedVideo(activity: Activity) {
        try {
            if (preloadedRewardedAd?.isReady == true || isRewardedLoading) {
                return
            }

            val ad = StartAppAd(activity)
            preloadedRewardedAd = ad
            isRewardedLoading = true

            Log.d(TAG, "Preloading Start.io Rewarded Video (AppId=$APP_ID, TestMode=$isTestMode)...")
            ad.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
                override fun onReceiveAd(receivedAd: Ad) {
                    isRewardedLoading = false
                    Log.d(TAG, "Start.io Rewarded Video successfully loaded and ready!")
                }

                override fun onFailedToReceiveAd(receivedAd: Ad?) {
                    isRewardedLoading = false
                    preloadedRewardedAd = null
                    Log.w(TAG, "Start.io Rewarded Video failed to preload: ${receivedAd?.errorMessage}")
                }
            })
        } catch (e: Exception) {
            isRewardedLoading = false
            preloadedRewardedAd = null
            Log.e(TAG, "Error during preloadRewardedVideo", e)
        }
    }

    fun preloadInterstitial(activity: Activity) {
        try {
            if (preloadedInterstitialAd?.isReady == true || isInterstitialLoading) {
                return
            }

            val ad = StartAppAd(activity)
            preloadedInterstitialAd = ad
            isInterstitialLoading = true

            Log.d(TAG, "Preloading Start.io Interstitial (AppId=$APP_ID, TestMode=$isTestMode)...")
            ad.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
                override fun onReceiveAd(receivedAd: Ad) {
                    isInterstitialLoading = false
                    Log.d(TAG, "Start.io Interstitial successfully loaded and ready!")
                }

                override fun onFailedToReceiveAd(receivedAd: Ad?) {
                    isInterstitialLoading = false
                    preloadedInterstitialAd = null
                    Log.w(TAG, "Start.io Interstitial failed to preload: ${receivedAd?.errorMessage}")
                }
            })
        } catch (e: Exception) {
            isInterstitialLoading = false
            preloadedInterstitialAd = null
            Log.e(TAG, "Error during preloadInterstitial", e)
        }
    }

    fun onBackPressed(activity: Activity) {
        try {
            StartAppAd.onBackPressed(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error in StartAppAd.onBackPressed", e)
        }
    }

    fun showInterstitialAd(
        activity: Activity,
        onAdDisplayed: () -> Unit = {},
        onAdClosed: () -> Unit = {}
    ) {
        try {
            val preloaded = preloadedInterstitialAd
            if (preloaded != null && preloaded.isReady) {
                preloadedInterstitialAd = null
                val displayed = preloaded.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }
                })
                if (displayed) return
            }

            loadAndShowInterstitial(activity, onAdDisplayed, onAdClosed, allowTestFallback = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying interstitial ad", e)
            activity.runOnUiThread { onAdClosed() }
        }
    }

    private fun loadAndShowInterstitial(
        activity: Activity,
        onAdDisplayed: () -> Unit,
        onAdClosed: () -> Unit,
        allowTestFallback: Boolean
    ) {
        val startAppAd = StartAppAd(activity)
        startAppAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                val displayed = startAppAd.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }
                })
                if (!displayed) {
                    activity.runOnUiThread { onAdClosed() }
                    preloadAds(activity)
                }
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                if (allowTestFallback && !isTestMode) {
                    Log.w(TAG, "Live interstitial no-fill on phone. Attempting test-mode fallback for device display...")
                    StartAppSDK.setTestAdsEnabled(true)
                    loadAndShowInterstitial(activity, onAdDisplayed, {
                        StartAppSDK.setTestAdsEnabled(isTestMode)
                        onAdClosed()
                    }, allowTestFallback = false)
                } else {
                    activity.runOnUiThread { onAdClosed() }
                    preloadAds(activity)
                }
            }
        })
    }

    /**
     * Shows a real Rewarded Ad to the user.
     * Strategy:
     * 1. Checks if preloaded pure Rewarded Video is ready.
     * 2. If not ready, checks if preloaded Fullscreen/Interstitial is ready.
     * 3. If neither is preloaded, triggers an on-demand request: first trying REWARDED_VIDEO,
     *    and if that has no instant video fill, seamlessly loading AUTOMATIC fullscreen.
     * 4. If live fill is 0 on phone (unreleased app), seamlessly falls back to test ad
     *    so that an actual ad plays on the physical phone!
     * 5. If all fail or no network fill, triggers onAdUnavailable with fallback reward mechanism.
     */
    fun showRewardedVideoAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDisplayed: () -> Unit = {},
        onAdClosed: () -> Unit = {},
        onAdUnavailable: (String) -> Unit = {}
    ) {
        try {
            var rewardGiven = false

            fun grantRewardOnce() {
                if (!rewardGiven) {
                    rewardGiven = true
                    activity.runOnUiThread { onRewardEarned() }
                }
            }

            // Case 1: Preloaded Rewarded Video is ready
            val preloadedVideo = preloadedRewardedAd
            if (preloadedVideo != null && preloadedVideo.isReady) {
                Log.d(TAG, "Showing preloaded Start.io Rewarded Video on device")
                preloadedRewardedAd = null

                preloadedVideo.setVideoListener(object : VideoListener {
                    override fun onVideoCompleted() {
                        Log.d(TAG, "Start.io Rewarded Video completed!")
                        grantRewardOnce()
                    }
                })

                val displayed = preloadedVideo.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        grantRewardOnce()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        Log.w(TAG, "Preloaded video adNotDisplayed")
                        grantRewardOnce()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }
                })

                if (displayed) return
            }

            // Case 2: Preloaded Interstitial/Fullscreen is ready
            val preloadedInter = preloadedInterstitialAd
            if (preloadedInter != null && preloadedInter.isReady) {
                Log.d(TAG, "Showing preloaded Start.io Interstitial for reward on device")
                preloadedInterstitialAd = null

                val displayed = preloadedInter.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        grantRewardOnce()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        grantRewardOnce()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }
                })

                if (displayed) return
            }

            // Case 3: On-demand load with multi-tier failover
            loadDynamicRewardAd(
                activity = activity,
                grantReward = { grantRewardOnce() },
                onAdDisplayed = onAdDisplayed,
                onAdClosed = onAdClosed,
                onAdUnavailable = onAdUnavailable,
                allowTestFallback = true
            )

        } catch (e: Exception) {
            Log.e(TAG, "Exception in showRewardedVideoAd", e)
            activity.runOnUiThread { onAdUnavailable(e.message ?: "Hata") }
        }
    }

    private fun loadDynamicRewardAd(
        activity: Activity,
        grantReward: () -> Unit,
        onAdDisplayed: () -> Unit,
        onAdClosed: () -> Unit,
        onAdUnavailable: (String) -> Unit,
        allowTestFallback: Boolean
    ) {
        val freshVideoAd = StartAppAd(activity)
        freshVideoAd.setVideoListener(object : VideoListener {
            override fun onVideoCompleted() {
                grantReward()
            }
        })

        freshVideoAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                Log.d(TAG, "On-demand video received! Showing on device...")
                val displayed = freshVideoAd.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        grantReward()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        grantReward()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }
                })
                if (!displayed) {
                    loadFallbackFullscreenAd(
                        activity, grantReward, onAdDisplayed, onAdClosed, onAdUnavailable, allowTestFallback
                    )
                }
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                Log.w(TAG, "Rewarded video fill not ready (${ad?.errorMessage}), falling back to fullscreen ad...")
                loadFallbackFullscreenAd(
                    activity, grantReward, onAdDisplayed, onAdClosed, onAdUnavailable, allowTestFallback
                )
            }
        })
    }

    private fun loadFallbackFullscreenAd(
        activity: Activity,
        grantReward: () -> Unit,
        onAdDisplayed: () -> Unit,
        onAdClosed: () -> Unit,
        onAdUnavailable: (String) -> Unit,
        allowTestFallback: Boolean
    ) {
        val fallbackAd = StartAppAd(activity)
        fallbackAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                Log.d(TAG, "Fallback real fullscreen ad received, showing now...")
                val displayed = fallbackAd.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        grantReward()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        grantReward()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }
                })

                if (!displayed) {
                    grantReward()
                    activity.runOnUiThread { onAdClosed() }
                    preloadAds(activity)
                }
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                if (allowTestFallback && !isTestMode) {
                    Log.w(TAG, "Live ads have no fill on this device. Switching to test ads to guarantee ad display on phone...")
                    StartAppSDK.setTestAdsEnabled(true)
                    loadDynamicRewardAd(
                        activity = activity,
                        grantReward = grantReward,
                        onAdDisplayed = onAdDisplayed,
                        onAdClosed = {
                            StartAppSDK.setTestAdsEnabled(isTestMode)
                            onAdClosed()
                        },
                        onAdUnavailable = { errMsg ->
                            StartAppSDK.setTestAdsEnabled(isTestMode)
                            activity.runOnUiThread { onAdUnavailable(errMsg) }
                        },
                        allowTestFallback = false
                    )
                } else {
                    val errMsg = ad?.errorMessage ?: "Bağlantı hatası"
                    Log.w(TAG, "Ad requests exhausted: $errMsg")
                    activity.runOnUiThread {
                        onAdUnavailable(errMsg)
                    }
                    preloadAds(activity)
                }
            }
        })
    }
}
