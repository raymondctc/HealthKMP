package com.ninegag.move.kmp.model

import com.tweener.firebase.firestore.model.FirestoreModel

class FirestoreUserModel(val user: User) : FirestoreModel() {
    override var id: String = ""
        get() = user.id
}