package com.yugalify.arrowzen.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Real Google Mobile Ads-backed implementation (Section 33). Only compiled
 * when ARROWZEN_ENABLE_ADMOB=true in gradle.properties -- see docs/ADMOB.md.
 *
 * Every SDK call is wrapped so a failure at any stage (network down, no
 * fill, SDK internal error) falls through to the same "unavailable"
 * callback path [NoOpAdManager] uses, rather than ever crashing or
 * blocking gameplay. Ad instances are cleared after use and immediately
 * re-requested, so the next opportunity already has a fresh ad loading in
 * the background.
 */
class GoogleMobileAdsManager(private val context: Context) : AdManager {

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var initialized = false

    init {
        runCatching {
            MobileAds.initialize(context) { initialized = true }
        }
    }

    override fun loadRewardedAd() {
        runCatching {
            RewardedAd.load(
                context,
                AdConfig.rewardedAdUnitId,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        rewardedAd = null
                    }
                }
            )
        }
    }

    override fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            onUnavailable()
            loadRewardedAd() // opportunistically fetch one for next time
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                onUnavailable()
                loadRewardedAd()
            }
        }

        runCatching {
            ad.show(activity) { onRewardEarned() }
        }.onFailure {
            onUnavailable()
        }
    }

    override fun loadInterstitialAd() {
        runCatching {
            InterstitialAd.load(
                context,
                AdConfig.interstitialAdUnitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
        }
    }

    override fun maybeShowInterstitial(activity: Activity, levelsCompletedSinceLastShown: Int) {
        if (levelsCompletedSinceLastShown < AdConfig.INTERSTITIAL_MIN_LEVELS_BETWEEN) return
        val ad = interstitialAd ?: run { loadInterstitialAd(); return }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                loadInterstitialAd()
            }
        }

        runCatching { ad.show(activity) }
    }
}
