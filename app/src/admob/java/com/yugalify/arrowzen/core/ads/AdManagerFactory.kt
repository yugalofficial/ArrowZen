package com.yugalify.arrowzen.core.ads

import android.content.Context

/**
 * AdMob-enabled build variant (ARROWZEN_ENABLE_ADMOB=true in
 * gradle.properties). Falls back to [NoOpAdManager] if ads are disabled at
 * runtime via [AdConfig.isAdsEnabled] even though the dependency is
 * present, so the two flags stay independently meaningful: one controls
 * whether the SDK is compiled in at all, the other controls whether it's
 * actually used.
 */
fun provideAdManager(context: Context): AdManager =
    if (AdConfig.isAdsEnabled) GoogleMobileAdsManager(context) else NoOpAdManager()
