package com.ninegag.move.kmp.repository

import com.ninegag.move.kmp.model.User
import com.ninegag.move.kmp.model.firestore.FirestoreUser
import com.tweener.firebase.firestore.FirestoreService
import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent

class MoveAppRepository(
    private val firestoreService: FirestoreService,
) : KoinComponent {

    suspend fun mayCreateUserCollection(
        user: User?,
        isAuthorized: Boolean
    ) {
        if (user === null || !isAuthorized) {
            Napier.v(tag = "FirestoreOp", message = "User is ${user}, isAuthorized=$isAuthorized, stop creating collection")
            return
        }

        try {
            // TODO: Library doesn't allow to return a nullable user to check existence, doing this a try-catch
            val fireStoreUser = firestoreService.get<FirestoreUser>("users", user!!.email)
            firestoreService.update(
                collection = "users",
                id = user!!.email,
                data = mapOf(
                    "username" to user!!.name,
                    "email" to user!!.email,
                    "avatarUrl" to user!!.avatarUrl
                )
            )
            Napier.v(tag = "FirestoreOp", message = "User is ${fireStoreUser}, user=${user}, isAuthorized=$isAuthorized, exists, stop creating collection")
        } catch (e: Exception) {
            Napier.v(tag = "FirestoreOp", message = "User is ${user}, isAuthorized=$isAuthorized, doesn't exists, now creating collection")
            firestoreService.create<FirestoreUser>("users", user!!.email, mapOf(
                "username" to user!!.name,
                "email" to user!!.email,
                "avatarUrl" to user!!.avatarUrl
            ))
        }
    }
}