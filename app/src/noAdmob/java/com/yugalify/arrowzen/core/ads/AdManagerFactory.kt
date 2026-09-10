package com.yugalify.arrowzen.core.ads

import android.content.Context

/**
 * Default build variant (ARROWZEN_ENABLE_ADMOB not set, or "false" in
 * gradle.properties): always returns the dependency-free [NoOpAdManager],
 * so the default build has zero third-party ad-SDK involvement at all.
 * See docs/ADMOB.md to switch to the real Google Mobile Ads-backed variant
 * in app/src/admob/java.
 */
fun provideAdManager(context: Context): AdManager = NoOpAdManager()
