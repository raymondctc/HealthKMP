package com.ninegag.moves.kmp

import androidx.lifecycle.ViewModel
import com.ninegag.moves.kmp.repository.MoveAppRepository
import com.ninegag.moves.kmp.model.ChallengePeriod
import com.ninegag.moves.kmp.model.StepTicketBucket
import com.ninegag.moves.kmp.model.StepTicketBucketUiValues
import com.ninegag.moves.kmp.model.StepsAndTicketsRecord
import com.ninegag.moves.kmp.model.User
import com.ninegag.moves.kmp.utils.numberOfDays
import com.ninegag.moves.kmp.utils.toDailyStepsDateString
import com.ninegag.moves.kmp.utils.toThousandSeparatedString
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import com.tweener.firebase.firestore.toTimestamp
import com.tweener.firebase.remoteconfig.datasource.RemoteConfigDataSource
import com.vitoksmile.kmp.health.HealthDataType
import com.vitoksmile.kmp.health.HealthManager
import com.vitoksmile.kmp.health.readSteps
import com.vitoksmile.kmp.health.records.StepsRecord
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

data class UiState(
    var user: User? = null,
    val isHealthManagerAvailable: Boolean,
    val isAuthorized: Boolean,
    val stepsRecord: Map<String, StepsAndTicketsRecord>,
    val currentDaySteps: Int,
    val dailyTargetSteps: Int, // minimum daily target
    val currentReward: Int, // calculated reward tickets
    val challengePeriod: ChallengePeriod,
    val stepTicketBucket: List<StepTicketBucketUiValues>
)

class MainViewModel(
    private val firebaseGoogleAuthProvider: FirebaseGoogleAuthProvider
) : ViewModel(), KoinComponent {
    private val readTypes = listOf<HealthDataType>(HealthDataType.Steps)
    private val writeTypes = listOf<HealthDataType>(HealthDataType.Steps)
    private val healthManager: HealthManager by inject()

    private val repository: MoveAppRepository by inject()
    private var isAuthorized: Boolean = false
    private var user: User? = null

    private val _uiState = MutableStateFlow(
        UiState(
            user = null,
            isHealthManagerAvailable = false,
            isAuthorized = false,
            stepsRecord = emptyMap(),
            currentDaySteps = 0,
            dailyTargetSteps = 0,
            currentReward = 0,
            challengePeriod = getChallengePeriod(),
            stepTicketBucket = emptyList()
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    suspend fun loadUser() = firebaseGoogleAuthProvider.getCurrentUser()?.also {
        val isHealthManagerAvailable = healthManager.isAvailable().getOrNull() ?: false
        isAuthorized = healthManager.isAuthorized(
            readTypes = readTypes,
            writeTypes = writeTypes
        ).getOrNull() ?: false
        user = User(
            id = it.uid,
            email = it.email ?: "",
            name = it.displayName ?: "",
            avatarUrl = it.photoUrl ?: ""
        )
        _uiState.emit(
            _uiState.value.copy(
                user = user,
                isHealthManagerAvailable = isHealthManagerAvailable,
                isAuthorized = isAuthorized
            )
        )

        repository.createOrUpdateUserCollection(user, isAuthorized)
    }

    suspend fun requestAuthorization() {
        try {
            val result = healthManager.requestAuthorization(readTypes = readTypes, writeTypes = writeTypes)
            result.onSuccess {
                isAuthorized = result.getOrDefault(false)
                _uiState.value = _uiState.value.copy(
                    isAuthorized = isAuthorized
                )
            }.onFailure {
                Napier.e(tag = "requestAuthorization", message = "error=${it.message}")
            }
        } catch (e: Exception) {
            Napier.e(tag = "requestAuthorization", message = "error=${e.message}")
        }
    }

    private suspend fun signInAsync(): Result<User> {
        val deferred = CompletableDeferred<Result<User>>()

        firebaseGoogleAuthProvider.signIn { result ->
            deferred.complete(result.map {
                User(
                    id = it.uid,
                    email = it.email ?: "",
                    name = it.displayName ?: "",
                    avatarUrl = it.photoUrl ?: ""
                )
            })
        }

        return deferred.await()
    }

    suspend fun signIn() = coroutineScope {
        val result = async { signInAsync() }.await()
        val isHealthManagerAvailable = healthManager.isAvailable().getOrNull() ?: false
        isAuthorized = healthManager.isAuthorized(
            readTypes = readTypes,
            writeTypes = writeTypes
        ).getOrNull() ?: false

        result.getOrNull()?.also { u ->
            Napier.v { "response, $u" }
            user = u
            _uiState.emit(
                _uiState.value.copy(
                    user = user,
                    isHealthManagerAvailable = isHealthManagerAvailable,
                    isAuthorized = isAuthorized
                )
            )
        }
    }

    suspend fun signOut() {
        firebaseGoogleAuthProvider.signOut()
        user = null
        _uiState.emit(
            _uiState.value.copy(
                user = null,
                stepsRecord = emptyMap(),
                currentDaySteps = 0,
                dailyTargetSteps = 0,
            )
        )
    }

    suspend fun loadStepCount() {
        if (!isAuthorized) {
            Napier.v(tag = "loadStepCount", message = "authorized=$isAuthorized, skip loading")
            return;
        }

        user?.let { u ->
            val todayStepCount = repository.getStepsForToday()
            val targetStepsList = repository.targetStepsList
            val rewardAndNextTarget = repository.getTicketsEarnedForSteps(todayStepCount)
            val currentReward = rewardAndNextTarget.first
            val nextTarget = rewardAndNextTarget.second

            _uiState.emit(
                _uiState.value.copy(
                    currentReward = currentReward,
                    currentDaySteps = todayStepCount,
                    dailyTargetSteps = nextTarget,
                    stepTicketBucket = targetStepsList.getValue().map { item ->
                        StepTicketBucketUiValues(
                            stepsMin = item.stepsMin.toThousandSeparatedString(),
                            stepsMax = item.stepsMax.toThousandSeparatedString(),
                            tickets = item.tickets.toString()
                        )
                    }
                )
            )

            val stepsMap = repository.getStepsFromStartOfMonthToToday()
            _uiState.emit(
                _uiState.value.copy(
                    stepsRecord = stepsMap
                )
            )

            repository.createOrUpdateStepsCollection(u, stepsMap)
            val prevMap = repository.getPrevWeekStepsBeforeStartOfTheMonth()
            repository.createOrUpdateStepsCollection(u, prevMap)
        }
    }


    private fun getChallengePeriod(): ChallengePeriod {
        val today = Clock.System.now()
        val localDateTime = today.toLocalDateTime(TimeZone.currentSystemDefault())
        val startOfMonth = LocalDate(localDateTime.year, localDateTime.monthNumber, 1)
        val endOfMonthDay = localDateTime.month.numberOfDays(localDateTime.year)
        val endOfMonth = LocalDate(localDateTime.year, localDateTime.monthNumber, endOfMonthDay)
        val challengePeriod = ChallengePeriod(
            start = startOfMonth,
            end = endOfMonth
        )
        return challengePeriod
    }



}