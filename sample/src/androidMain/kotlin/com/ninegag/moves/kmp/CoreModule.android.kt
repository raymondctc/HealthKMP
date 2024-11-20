package com.ninegag.moves.kmp

import android.content.Context
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import org.koin.core.module.Module
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProviderAndroid
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.getKoin

actual fun platformGoogleAuthModule(serverClientId: String): Module = module {
    val context: Context = getKoin().get()
    single<FirebaseGoogleAuthProvider> {
        FirebaseGoogleAuthProviderAndroid(context = context, serverClientId = serverClientId)
    }
}