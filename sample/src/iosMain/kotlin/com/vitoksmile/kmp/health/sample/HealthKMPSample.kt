@file:Suppress("unused", "FunctionName")

package com.vitoksmile.kmp.health.sample

import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ninegag.move.kmp.MainViewModel
import com.ninegag.move.kmp.firestoreModule
import com.ninegag.move.kmp.ui.MoveApp
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProviderIos
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
                firestoreModule
            )
        }
    }

    fun MainViewController() = ComposeUIViewController {
        val authProvider = FirebaseGoogleAuthProviderIos(serverClientId = "1:233233604356:ios:95529c1ce0dc7e0752fa51")
        MoveApp(
            viewModel = viewModel { MainViewModel(authProvider) }
        )
    }
}