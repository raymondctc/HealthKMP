package com.ninegag.move.kmp

import android.content.Context
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import org.koin.core.module.Module
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProviderAndroid
import org.koin.core.definition.KoinDefinition
import org.koin.java.KoinJavaComponent.getKoin

actual fun platformGoogleAuthModule(
    module: Module,
    serverClientId: String
): KoinDefinition<FirebaseGoogleAuthProvider> = with(module) {
    val context: Context = getKoin().get()
    single<FirebaseGoogleAuthProvider> {
        FirebaseGoogleAuthProviderAndroid(context = context, serverClientId = serverClientId)
    }
}