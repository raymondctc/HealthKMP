package com.ninegag.moves.kmp

import androidx.lifecycle.ViewModel
import com.ninegag.moves.kmp.repository.MoveAppRepository
import com.ninegag.moves.kmp.model.ChallengePeriod
import com.ninegag.moves.kmp.model.StepTicketBucket
import com.ninegag.moves.kmp.model.User
import com.ninegag.moves.kmp.utils.numberOfDays
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.days

data class UiState(
    var user: User? = null,
    val isHealthManagerAvailable: Boolean,
    val isAuthorized: Boolean,
    val stepsRecord: Map<String, Int>,
    val currentDaySteps: Int,
    val dailyTargetSteps: Int, // minimum daily target
    val currentReward: Int, // calculated reward tickets
    val challengePeriod: ChallengePeriod
)

class MainViewModel(
    private val firebaseGoogleAuthProvider: FirebaseGoogleAuthProvider
) : ViewModel(), KoinComponent {
    private val readTypes = listOf<HealthDataType>(HealthDataType.Steps)
    private val writeTypes = emptyList<HealthDataType>()
    private val healthManager: HealthManager by inject()

    private val repository: MoveAppRepository by inject()
    private val remoteConfig: RemoteConfigDataSource by inject()
    private var targetStepsList: List<StepTicketBucket> = emptyList()
    private var stepsList: Map<String, Int>? = null
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
            challengePeriod = getChallengePeriod()
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
        _uiState.value = UiState(
            user,
            isHealthManagerAvailable,
            isAuthorized,
            if (stepsList != null) stepsList!! else emptyMap(),
            _uiState.value.currentDaySteps,
            _uiState.value.dailyTargetSteps,
            _uiState.value.currentReward,
            _uiState.value.challengePeriod
        )
        loadDailyTarget()
        loadDailyReward()
        loadStepCount()
    }

    suspend fun requestAuthorization() {
        try {
            val result = healthManager.requestAuthorization(readTypes = readTypes, writeTypes = writeTypes)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isAuthorized = result.getOrDefault(false))
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

        stepsList = getSummedStepsCountForMonth(isAuthorized)

        result.getOrNull()?.also { u ->
            Napier.v { "response, $u" }
            user = u
            _uiState.emit(
                UiState(
                    user = user,
                    isHealthManagerAvailable = isHealthManagerAvailable,
                    isAuthorized = isAuthorized,
                    stepsRecord = if (stepsList != null) stepsList!! else emptyMap(),
                    currentDaySteps = _uiState.value.currentDaySteps,
                    dailyTargetSteps = _uiState.value.dailyTargetSteps,
                    currentReward = _uiState.value.currentReward,
                    challengePeriod = getChallengePeriod()
                ),
            )
        }
        loadUser()
    }

    suspend fun signOut() {
        firebaseGoogleAuthProvider.signOut()
        user = null
        _uiState.emit(
            UiState(
                user = null,
                isHealthManagerAvailable = false,
                isAuthorized = false,
                stepsRecord = emptyMap(),
                currentDaySteps = 0,
                dailyTargetSteps = 0,
                challengePeriod = getChallengePeriod(),
                currentReward = 0
            )
        )
    }

    suspend fun loadStepCount() {
        if (!isAuthorized) {
            Napier.v(tag = "loadStepCount", message = "authorized=$isAuthorized, skip loading")
            return;
        }
        val todaySteps = repository.getTodaySteps()
        stepsList = getSummedStepsCountForMonth(isAuthorized)
        val newState = _uiState.value.copy(
            stepsRecord = if (stepsList != null) stepsList!! else emptyMap(),
            currentDaySteps = todaySteps
        )
        _uiState.emit(newState)

        repository.createOrUpdateStepsCollection(user!!)
        // debug log
        Napier.v(tag = "loadStepCount", message = "Loaded step count: todaySteps=$todaySteps, monthlySteps=${stepsList?.values?.sum()}")
    }

    suspend fun loadDailyReward() {
        val dailyTarget = remoteConfig.getString(
            Constants.RemoteConfigKeys.DAILT_TARGET_TICKET,
            Constants.RemoteConfigDefaults.DEFAULT_TARGET_TICKET
        )

        targetStepsList = Json.decodeFromString<List<StepTicketBucket>>(dailyTarget)
        val currentDaySteps = _uiState.value.currentDaySteps

        // Determine reward tickets based on currentDaySteps
        var currentReward = 0
        for (target in targetStepsList) {
            if (currentDaySteps in target.stepsMin..target.stepsMax) {
                currentReward = target.tickets
                break
            }
        }

        _uiState.emit(
            _uiState.value.copy(
                currentReward = currentReward
            )
        )

        // debug log
        Napier.v {
            "loadDailyReward(): currentDaySteps=$currentDaySteps, currentReward=$currentReward"
        }
    }

    suspend fun loadDailyTarget() {
        try {
            val target = repository.getMinStepTarget()
            _uiState.emit(
                _uiState.value.copy(
                    dailyTargetSteps = target
                )
            )
            Napier.v { "Successfully loaded daily target: $target" }
        } catch (e: Exception) {
            Napier.e(tag = "loadDailyTarget", message = "Error loading daily target: ${e.message}")
        }
    }

    suspend fun mayCreateUserDoc() {
        mayCreateUserCollection()
    }

    private suspend fun getSummedStepsCountForMonth(isAuthorized: Boolean): Map<String, Int>? {
        if (!isAuthorized) {
            return null
        }
        val linkedHashMap = LinkedHashMap<String, Int>()
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val startOfMonth = LocalDate(localDateTime.year, localDateTime.monthNumber, 1).atStartOfDayIn(TimeZone.currentSystemDefault())
        val diff = localDateTime.toInstant(TimeZone.currentSystemDefault()).minus(startOfMonth)
        val daysDuration = diff.inWholeDays.days

        for (i in daysDuration.inWholeDays downTo 0) {
            Napier.v(tag = "getSummedStepsCountForMonth", message = "i=$i, diff=${daysDuration.inWholeDays}")
            val curr = now.minus(i.days).toLocalDateTime(TimeZone.currentSystemDefault())
            val todaysDate = LocalDate(curr.year, curr.monthNumber, curr.dayOfMonth)
            val start = todaysDate.atStartOfDayIn(TimeZone.currentSystemDefault())
            val end = todaysDate.atTime(hour = 23, minute = 59, second = 59, nanosecond = 999999999)
                .toInstant(TimeZone.currentSystemDefault())
            val dateString = "${curr.year}${curr.monthNumber}${curr.dayOfMonth}"

            val stepList = healthManager.readSteps(start, end)
            val stepCounts = stepList.getOrDefault(emptyList())

            linkedHashMap[dateString] = stepCounts.sumOf { it.count }
        }

        return linkedHashMap
    }

    /**
     * To create user doc on Firebase if not exists
     */
    private suspend fun mayCreateUserCollection() {
        repository.createOrUpdateUserCollection(user, isAuthorized)
    }
}