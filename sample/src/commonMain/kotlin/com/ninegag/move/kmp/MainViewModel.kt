package com.ninegag.move.kmp

import androidx.lifecycle.ViewModel
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import com.vitoksmile.kmp.health.records.StepsRecord
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class User(
    val id: String,
    val email: String,
    val name: String
)

data class UiState(
    var user: User? = null,
    val stepsRecord: Map<String, StepsRecord>
)

class MainViewModel : ViewModel(), KoinComponent {
    private val firebaseGoogleAuthProvider: FirebaseGoogleAuthProvider by inject()

    private val _uiState = MutableStateFlow(UiState(null, emptyMap()))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadUser() = firebaseGoogleAuthProvider.getCurrentUser()?.also {
        _uiState.value = UiState(User(it.uid, it.email ?: "", it.displayName ?: ""), emptyMap())
    }

    private suspend fun signInAsync(): Result<User> {
        val deferred = CompletableDeferred<Result<User>>()

        firebaseGoogleAuthProvider.signIn { result ->
            deferred.complete(result.map {
                User(it.uid, it.email ?: "", it.displayName ?: "")
            })
        }

        return deferred.await()
    }

    suspend fun signIn() = coroutineScope {
        val result = async { signInAsync() }.await()
        result.getOrNull()?.also { user ->
            Napier.v { "response, $user" }
            _uiState.emit(UiState(user, emptyMap()))
        }
    }

}