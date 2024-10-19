package com.ninegag.move.kmp.model

import com.tweener.firebase.firestore.model.FirestoreModel
import kotlinx.serialization.Serializable

@Serializable
data class FirestoreUserModel(
    override var id: String,
    val name: String,
    val step_count: Map<String, Int>
) : FirestoreModel()

@Serializable
data class FirestoreUserModel2(
    override var id: String,
    val count: Int
): FirestoreModel()