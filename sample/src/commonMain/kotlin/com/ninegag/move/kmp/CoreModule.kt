package com.ninegag.move.kmp

import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module

expect fun platformGoogleAuthModule(serverClientId: String): Module