package com.ninegag.move.kmp

import androidx.lifecycle.ViewModel
import com.ninegag.move.kmp.model.FirestoreUserModel2
import com.ninegag.move.kmp.model.User
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import com.tweener.firebase.firestore.FirestoreService
import com.tweener.firebase.firestore.model.FirestoreModel
import com.vitoksmile.kmp.health.HealthDataType
import com.vitoksmile.kmp.health.HealthManagerFactory
import com.vitoksmile.kmp.health.readSteps
import com.vitoksmile.kmp.health.records.StepsRecord
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class UiState(
    var user: User? = null,
    val isHealthManagerAvailable: Boolean,
    val isAuthorized: Boolean,
    val stepsRecord: Map<String, StepsRecord>
)

class MainViewModel : ViewModel(), KoinComponent {
    private val readTypes = listOf(HealthDataType.Steps)
    private val writeTypes = listOf(HealthDataType.Steps)
    private val healthManagerFactory: HealthManagerFactory by inject()
    private val healthManager = healthManagerFactory.createManager()
    private val firebaseGoogleAuthProvider: FirebaseGoogleAuthProvider by inject()
    private val firestoreService: FirestoreService by inject()

    private val _uiState = MutableStateFlow(
        UiState(null, isHealthManagerAvailable = false, isAuthorized = false, emptyMap())
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    suspend fun loadUser() = firebaseGoogleAuthProvider.getCurrentUser()?.also {
        val isHealthManagerAvailable = healthManager.isAvailable().getOrNull() ?: false
        val isAuthorized = healthManager.isAuthorized(
            readTypes = readTypes,
            writeTypes = writeTypes
        ).getOrNull() ?: false
        _uiState.value = UiState(
            User(it.uid, it.email ?: "", it.displayName ?: ""),
            isHealthManagerAvailable,
            isAuthorized,
            emptyMap()
        )
    }

    suspend fun requestAuthorization() {
        val result = healthManager.requestAuthorization(readTypes = readTypes, writeTypes = writeTypes)
        _uiState.value = _uiState.value.copy(isAuthorized = result.getOrNull() ?: false)
    }

    private suspend fun signInAsync(): Result<User> {
        val deferred = CompletableDeferred<Result<User>>()

        firebaseGoogleAuthProvider.signIn { result ->
            deferred.complete(result.map {
                User(it.uid, it.email ?: "", it.displayName ?: "")
            })
        }

        return deferred.await()
    }

    suspend fun signIn() = coroutineScope {
        val result = async { signInAsync() }.await()
        val isHealthManagerAvailable = healthManager.isAvailable().getOrNull() ?: false
        val isAuthorized = healthManager.isAuthorized(
            readTypes = readTypes,
            writeTypes = writeTypes
        ).getOrNull() ?: false

        result.getOrNull()?.also { user ->
            Napier.v { "response, $user" }
            _uiState.emit(UiState(user, isHealthManagerAvailable, isAuthorized = isAuthorized, emptyMap()))
        }
    }

    suspend fun mayCreateUserDoc() {
        val user = loadUser() ?: return
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val todaysDate = LocalDate(now.year, now.monthNumber, now.dayOfMonth)
        val start = todaysDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val end = todaysDate.atTime(hour = 23, minute = 59, second = 59, nanosecond = 999999999)
            .toInstant(TimeZone.currentSystemDefault())

        val collectionString = "${now.year}${now.monthNumber}${now.dayOfMonth}"
        val stepList = healthManager.readSteps(start, end)
        val stepCounts = stepList.getOrDefault(emptyList())
        val data = mapOf("count" to stepCounts.sumOf { it.count })

        val docString = "${user.email}/${collectionString}/step_count"

        val isDocumentExists = Firebase.firestore.document("users/${docString}").get().exists
        if (!isDocumentExists) {
            Napier.v(tag = "FirestoreOp", message = "Document does not exist, writing now docString=${docString} data=${data}, stepsCount=${stepCounts}")
            firestoreService.create<FirestoreUserModel2>(
                collection = "users",
                id = docString,
                data = data
            )
        } else {
            Napier.v(tag = "FirestoreOp", message = "Document exists, docString=${docString}, writing data=${data}, stepsCount=${stepCounts}")
            firestoreService.update(
                collection = "users",
                id = docString,
                data = data
            )
        }
    }

}