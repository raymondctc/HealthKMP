package com.ninegag.moves.kmp

import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProviderIos
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformGoogleAuthModule(serverClientId: String): Module = module {
    single<FirebaseGoogleAuthProvider> {
        FirebaseGoogleAuthProviderIos(serverClientId = serverClientId)
    }
}