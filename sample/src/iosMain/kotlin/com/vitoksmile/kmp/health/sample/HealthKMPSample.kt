@file:Suppress("unused", "FunctionName")

package com.vitoksmile.kmp.health.sample

import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ninegag.moves.kmp.MainViewModel
import com.ninegag.moves.kmp.firebaseRemoteConfigModule
import com.ninegag.moves.kmp.firestoreModule
import com.ninegag.moves.kmp.healthManagerModule
import com.ninegag.moves.kmp.repositoryModule
import com.ninegag.moves.kmp.ui.MoveApp
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProviderIos
import com.vitoksmile.kmp.health.HealthManagerFactory
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.Foundation.dictionaryWithContentsOfFile

object HealthKMPSample {
    object GoogleServiceInfo {
        private val cachedValues: Map<String, String> by lazy {
            loadPlistValues()
        }

        private fun loadPlistValues(): Map<String, String> {
            val mainBundle = NSBundle.mainBundle
            val path = mainBundle.pathForResource("GoogleService-Info", "plist") ?: error("GoogleService-Info.plist not found")
            val dict = NSDictionary.dictionaryWithContentsOfFile(path) as? Map<*, *>
                ?: error("Failed to load GoogleService-Info.plist")
            return dict.filterKeys { it is String }.mapKeys { (key, _) -> key as String }
                .filterValues { it is String }.mapValues { (_, value) -> value as String }
        }

        fun getValue(key: String): String? = cachedValues[key]
    }

    fun start() {
        Napier.base(DebugAntilog())
        Napier.d { "Debug log, started!" }

        val applicationId = GoogleServiceInfo.getValue("GOOGLE_APP_ID") ?: error("GOOGLE_APP_ID not found")
        val apiKey = GoogleServiceInfo.getValue("API_KEY") ?: error("API_KEY not found")
        val projectId = GoogleServiceInfo.getValue("PROJECT_ID") ?: error("PROJECT_ID not found")
        val gcmSenderId = GoogleServiceInfo.getValue("GCM_SENDER_ID") ?: error("GCM_SENDER_ID not found")

        Napier.d { "applicationId=${applicationId}, apiKey=${apiKey}, projectId=${projectId}, gcmSenderId=${gcmSenderId}" }

        Firebase.initialize(
            options = FirebaseOptions(
                applicationId = applicationId,
                apiKey = apiKey,
                projectId = projectId,
                gcmSenderId = gcmSenderId
            )
        )
        startKoin {
            modules(
                module {
                    single { HealthManagerFactory() }
                },
                firestoreModule,
                firebaseRemoteConfigModule,
                repositoryModule,
                healthManagerModule
            )
        }
    }

    fun MainViewController() = ComposeUIViewController {
        val applicationId = GoogleServiceInfo.getValue("GOOGLE_APP_ID") ?: error("GOOGLE_APP_ID not found")
        val authProvider = FirebaseGoogleAuthProviderIos(
            serverClientId = applicationId
        )
        MoveApp(
            viewModel = viewModel { MainViewModel(authProvider) }
        )
    }
}