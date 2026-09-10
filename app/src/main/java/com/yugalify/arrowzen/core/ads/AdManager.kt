package com.yugalify.arrowzen.core.ads

import android.app.Activity

/**
 * Section 33 abstraction. The app must remain fully playable regardless of
 * which implementation is behind this interface -- AdMob unavailable,
 * network unavailable, or an ad simply failing to load must never block
 * gameplay. Every method here reports failure through a callback rather
 * than throwing, so callers never need a try/catch of their own.
 */
interface AdManager {
    fun loadRewardedAd()
    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onUnavailable: () -> Unit)

    fun loadInterstitialAd()
    fun maybeShowInterstitial(activity: Activity, levelsCompletedSinceLastShown: Int)
}

/**
 * Default, dependency-free implementation: reports every ad as unavailable
 * without ever touching a third-party SDK. This is what the app ships with
 * out of the box (see docs/ADMOB.md for how to switch to the real
 * Google Mobile Ads-backed implementation), and it's also the automatic
 * fallback shape everything else is designed around -- "no ads" is always
 * a completely valid, fully-functional state for this game.
 */
class NoOpAdManager : AdManager {
    override fun loadRewardedAd() = Unit
    override fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onUnavailable: () -> Unit) = onUnavailable()
    override fun loadInterstitialAd() = Unit
    override fun maybeShowInterstitial(activity: Activity, levelsCompletedSinceLastShown: Int) = Unit
}
