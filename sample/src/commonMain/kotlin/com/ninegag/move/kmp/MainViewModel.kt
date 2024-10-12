package com.ninegag.move.kmp

import androidx.lifecycle.ViewModel
import com.tweener.firebase.auth.FirebaseAuthService
import com.tweener.firebase.auth.datasource.FirebaseAuthDataSource
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import dev.gitlive.firebase.auth.GoogleAuthProvider

class MainViewModel : ViewModel() {
    fun signIn() {
        val firebaseAuthDataSource = FirebaseAuthDataSource(firebaseAuthService = FirebaseAuthService())

    }
}