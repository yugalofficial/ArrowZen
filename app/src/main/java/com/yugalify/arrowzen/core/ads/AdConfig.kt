package com.yugalify.arrowzen.core.ads

import com.yugalify.arrowzen.BuildConfig

/**
 * Section 70: centralized, configurable ad settings. Test ad unit IDs
 * (Google's official public test IDs, safe to ship and safe to compile
 * with -- these are documented at
 * https://developers.google.com/admob/android/test-ads) are used whenever
 * a release-specific ID hasn't been supplied via gradle.properties. This
 * means the project builds and runs correctly out of the box, in CI,
 * before anyone has created a real AdMob account.
 *
 * To go live: replace the values of ARROWZEN_ADMOB_* in gradle.properties
 * with your real AdMob unit IDs before a production release. See
 * docs/ADMOB.md.
 */
object AdConfig {
    /** Master switch. False disables all ad code paths (Section 33: app must work with ads unavailable). */
    val isAdsEnabled: Boolean = BuildConfig.ADS_ENABLED

    val appId: String = BuildConfig.ADMOB_APP_ID.ifBlank { TEST_APP_ID }
    val rewardedAdUnitId: String = BuildConfig.ADMOB_REWARDED_UNIT_ID.ifBlank { TEST_REWARDED_UNIT_ID }
    val interstitialAdUnitId: String = BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID.ifBlank { TEST_INTERSTITIAL_UNIT_ID }
    val bannerAdUnitId: String = BuildConfig.ADMOB_BANNER_UNIT_ID.ifBlank { TEST_BANNER_UNIT_ID }

    /** Minimum completed levels between interstitials (Section 33: show conservatively). */
    const val INTERSTITIAL_MIN_LEVELS_BETWEEN = 4

    // Google's official public test IDs -- safe for development and CI.
    private const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    private const val TEST_REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
}
