package com.yugalify.arrowzen

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.yugalify.arrowzen.core.di.AppContainer
import kotlinx.coroutines.launch

/**
 * ArrowZen application entry point. Owns the single [AppContainer] instance
 * for the process (Section 68's manual-DI approach).
 */
class ArrowZenApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Achievement rows must exist before any screen tries to observe or
        // increment them; safe to call on every launch (IGNORE on conflict).
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            container.achievementRepository.seedIfNeeded()
        }
    }
}
