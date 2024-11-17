package com.ninegag.move.kmp.model.firestore

import com.tweener.firebase.firestore.model.FirestoreModel
import kotlinx.serialization.Serializable

@Serializable
data class FirestoreDailyRank(
    override var id: String,
    val username: String,
    val email: String,
    val avatarUrl: String,
    val steps: Int,
): FirestoreModel()

@Serializable
data class FirestoreWeeklyRank(
    override var id: String,
    val username: String,
    val email: String,
    val avatarUrl: String,
    val steps: Int,
): FirestoreModel()

@Serializable
data class FirestoreMonthlyRank(
    override var id: String,
    val username: String,
    val email: String,
    val avatarUrl: String,
    val steps: Int,
): FirestoreModel()