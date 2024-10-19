package com.ninegag.move.app

import android.app.Application
import com.ninegag.move.kmp.firestoreModule
import com.ninegag.move.kmp.platformGoogleAuthModule
import com.vitoksmile.kmp.health.koin.attachHealthKMP
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        Napier.base(DebugAntilog())

        startKoin {
            androidContext(this@MainApplication)
            androidLogger()
            attachHealthKMP(this@MainApplication)
            modules(
                platformGoogleAuthModule(getString(R.string.default_web_client_id)),
                firestoreModule
            )
        }
    }
}