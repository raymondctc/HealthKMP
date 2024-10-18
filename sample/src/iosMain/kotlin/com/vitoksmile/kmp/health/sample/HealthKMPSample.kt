@file:Suppress("unused", "FunctionName")

package com.vitoksmile.kmp.health.sample

import androidx.compose.ui.window.ComposeUIViewController
import com.ninegag.move.kmp.platformGoogleAuthModule
import com.ninegag.move.kmp.ui.MoveApp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin

object HealthKMPSample {
    fun start() {
        Napier.base(DebugAntilog())
        Napier.d { "Debug log, started!" }
        Firebase.initialize(options = FirebaseOptions(
            applicationId = "1:233233604356:ios:95529c1ce0dc7e0752fa51",
            apiKey = "AIzaSyA6cI-ouqP_5Gh65ojdKV6C869guvaN_nc",
            projectId = "ninegag-move-test",
        ))
        startKoin {
            modules(
                platformGoogleAuthModule("1:233233604356:ios:95529c1ce0dc7e0752fa51")
            )
        }
    }

    fun MainViewController() = ComposeUIViewController {
        MoveApp()
    }
}