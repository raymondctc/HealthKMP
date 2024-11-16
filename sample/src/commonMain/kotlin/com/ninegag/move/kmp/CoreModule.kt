package com.ninegag.move.kmp


import com.tweener.firebase.firestore.FirestoreService
import com.tweener.firebase.remoteconfig.RemoteConfigService
import com.tweener.firebase.remoteconfig.datasource.RemoteConfigDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

expect fun platformGoogleAuthModule(serverClientId: String): Module

val firestoreModule: Module = module {
    single<FirestoreService> { FirestoreService() }
}

val firebaseRemoteConfigModule: Module = module {
    single<RemoteConfigDataSource> {
        val service = RemoteConfigService(true)
        RemoteConfigDataSource(firebaseRemoteConfigService = service)
    }
}