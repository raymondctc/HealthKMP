package com.ninegag.move.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.ninegag.move.kmp.ui.MoveApp
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProviderAndroid

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val j = FirebaseGoogleAuthProviderAndroid(context = this, serverClientId = getString(R.string.default_web_client_id))
        super.onCreate(savedInstanceState)
        setContent {
            MoveApp()
        }
    }
}