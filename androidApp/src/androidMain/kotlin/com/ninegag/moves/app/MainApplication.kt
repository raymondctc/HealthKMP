package com.ninegag.moves.app

import android.app.Application
import com.ninegag.moves.kmp.firebaseRemoteConfigModule
import com.ninegag.moves.kmp.firestoreModule
import com.ninegag.moves.kmp.healthManagerModule
import com.ninegag.moves.kmp.repositoryModule
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
                firestoreModule,
                firebaseRemoteConfigModule,
                repositoryModule,
                healthManagerModule
            )
        }
    }
}