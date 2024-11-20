package com.ninegag.moves.kmp.model

import com.tweener.firebase.firestore.model.FirestoreModel
import kotlinx.serialization.Serializable

@Deprecated("Will need to redesign a new user model")
@Serializable
data class FirestoreUserModel(
    override var id: String,
    val name: String,
    val step_count: Map<String, Int>
) : FirestoreModel()

@Deprecated("Will need to redesign a new user model")
@Serializable
data class FirestoreUserModel2(
    override var id: String,
    val count: Int
): FirestoreModel()