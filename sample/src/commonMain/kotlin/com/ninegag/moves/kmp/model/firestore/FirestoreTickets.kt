package com.ninegag.moves.kmp.model.firestore

import com.tweener.firebase.firestore.model.FirestoreModel
import kotlinx.serialization.Serializable

@Serializable
data class FirestoreTickets(
    override var id: String,
    val email: String,
    val totalTicketsEarned: Int
): FirestoreModel()