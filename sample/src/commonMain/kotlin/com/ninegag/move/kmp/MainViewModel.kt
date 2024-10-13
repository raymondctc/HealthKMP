package com.ninegag.move.kmp

import androidx.lifecycle.ViewModel
import com.tweener.firebase.auth.FirebaseUser
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import com.vitoksmile.kmp.health.records.StepsRecord
import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class UiState(
    var user: FirebaseUser? = null,
    val stepsRecord: Map<String, StepsRecord>
)

class MainViewModel : ViewModel(), KoinComponent {
    private val firebaseGoogleAuthProvider: FirebaseGoogleAuthProvider by inject()


    suspend fun signIn() = firebaseGoogleAuthProvider.signIn { response ->
        Napier.v { "response, ${response.getOrNull()}" }
    }
}