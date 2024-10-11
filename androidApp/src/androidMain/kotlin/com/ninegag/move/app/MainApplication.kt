package com.ninegag.move.app

import android.app.Application
import com.vitoksmile.kmp.health.koin.attachHealthKMP
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        startKoin {
            androidContext(this@MainApplication)
            androidLogger()
            attachHealthKMP(this@MainApplication)
        }
    }
}