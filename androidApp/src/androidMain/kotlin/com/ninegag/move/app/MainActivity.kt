package com.ninegag.move.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ninegag.move.kmp.MainViewModel
import com.ninegag.move.kmp.ui.MoveApp
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProviderAndroid

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authProvider = FirebaseGoogleAuthProviderAndroid(
            context = this, serverClientId = getString(R.string.default_web_client_id)
        )
        val viewModelFactory = MainViewModelFactory(authProvider)

        setContent {
            MoveApp(viewModelFactory.create(MainViewModel::class.java))
        }
    }
}

class MainViewModelFactory(
    private val authProvider: FirebaseGoogleAuthProvider
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(authProvider) as T
    }
}