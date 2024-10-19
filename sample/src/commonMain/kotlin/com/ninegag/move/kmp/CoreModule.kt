package com.ninegag.move.kmp

import com.tweener.firebase.firestore.FirestoreService
import org.koin.core.module.Module
import org.koin.dsl.module

expect fun platformGoogleAuthModule(serverClientId: String): Module

val firestoreModule: Module = module {
    single<FirestoreService> { FirestoreService() }
}