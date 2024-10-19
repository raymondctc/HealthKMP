@file:Suppress("unused", "FunctionName")

package com.vitoksmile.kmp.health.sample

import androidx.compose.ui.window.ComposeUIViewController
import com.ninegag.move.kmp.firestoreModule
import com.ninegag.move.kmp.platformGoogleAuthModule
import com.ninegag.move.kmp.ui.MoveApp
import com.vitoksmile.kmp.health.HealthManagerFactory
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.module

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
                module {
                    single { HealthManagerFactory() }
                },
                platformGoogleAuthModule("1:233233604356:ios:95529c1ce0dc7e0752fa51"),
                firestoreModule
            )
        }
    }

    fun MainViewController() = ComposeUIViewController {
        MoveApp()
    }
}