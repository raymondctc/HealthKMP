@file:Suppress("unused", "FunctionName")

package com.vitoksmile.kmp.health.sample

import androidx.compose.ui.window.ComposeUIViewController
import com.ninegag.move.kmp.platformGoogleAuthModule
import com.ninegag.move.kmp.ui.MoveApp
import org.koin.core.context.startKoin

object HealthKMPSample {
    fun start() {
        startKoin {
            modules(platformGoogleAuthModule("233233604356-atbu1ckn6uh00dj87ufch6a7s30u9mas.apps.googleusercontent.com"))
        }
    }

    fun MainViewController() = ComposeUIViewController {
        MoveApp()
    }
}