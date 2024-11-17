package com.ninegag.move.kmp.model.firestore

import com.tweener.firebase.firestore.model.FirestoreModel
import kotlinx.serialization.Serializable

@Serializable
data class FirestoreUser(
    override var id: String,
    val username: String,
    val email: String,
    val avatarUrl: String,
): FirestoreModel()