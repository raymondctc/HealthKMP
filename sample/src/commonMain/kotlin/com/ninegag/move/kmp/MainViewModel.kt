package com.ninegag.move.kmp

import androidx.lifecycle.ViewModel
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel : ViewModel(), KoinComponent {
    val firebaseGoogleAuthProvider: FirebaseGoogleAuthProvider by inject()
    suspend fun signIn() = firebaseGoogleAuthProvider.signIn { response ->
        Napier.v { "response, ${response.getOrNull()}" }
    }
}