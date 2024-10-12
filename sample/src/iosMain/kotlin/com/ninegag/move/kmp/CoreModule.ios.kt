package com.ninegag.move.kmp

import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProviderIos
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module

actual fun platformGoogleAuthModule(
    module: Module,
    serverClientId: String
): KoinDefinition<FirebaseGoogleAuthProvider> = with(module) {
    single<FirebaseGoogleAuthProvider> {
        FirebaseGoogleAuthProviderIos(serverClientId = serverClientId)
    }
}