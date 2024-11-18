package com.ninegag.move.kmp

import androidx.lifecycle.ViewModel
import com.ninegag.move.kmp.repository.MoveAppRepository
import com.ninegag.move.kmp.model.ChallengePeriod
import com.ninegag.move.kmp.model.FirestoreUserModel2
import com.ninegag.move.kmp.model.StepTicketBucket
import com.ninegag.move.kmp.model.User
import com.ninegag.move.kmp.utils.numberOfDays
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import com.tweener.firebase.firestore.FirestoreService
import com.tweener.firebase.remoteconfig.datasource.RemoteConfigDataSource
import com.vitoksmile.kmp.health.HealthDataType
import com.vitoksmile.kmp.health.HealthManager
import com.vitoksmile.kmp.health.readSteps
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

data class UiState(
    var user: User? = null,
    val isHealthManagerAvailable: Boolean,
    val isAuthorized: Boolean,
    val stepsRecord: Map<String, Int>,
    val challengePeriod: ChallengePeriod
)

class MainViewModel(
    private val firebaseGoogleAuthProvider: FirebaseGoogleAuthProvider
) : ViewModel(), KoinComponent {
    private val readTypes = listOf<HealthDataType>(HealthDataType.Steps)
    private val writeTypes = emptyList<HealthDataType>()
    private val healthManager: HealthManager by inject()

    @Deprecated("Moved to repository")
    private val firestoreService: FirestoreService by inject()
    private val repository: MoveAppRepository by inject()
    private val remoteConfig: RemoteConfigDataSource by inject()
    private var stepsList: Map<String, Int>? = null
    private var isAuthorized: Boolean = false
    private var user: User? = null

    private val _uiState = MutableStateFlow(
        UiState(
            user = null,
            isHealthManagerAvailable = false,
            isAuthorized = false,
            stepsRecord = emptyMap(),
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
            _uiState.value.challengePeriod
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
                    challengePeriod = getChallengePeriod()
                ),
            )
        }
    }

    suspend fun loadStepCount() {
        stepsList = getSummedStepsCountForMonth(isAuthorized)
        val newState = _uiState.value.copy(
            stepsRecord = if (stepsList != null) stepsList!! else emptyMap()
        )
        _uiState.emit(newState)

        repository.createOrUpdateStepsCollection(user!!)
    }

    /**
     * TODO: to get daily target from remote config
     */
    suspend fun loadDailyTarget() {
        val dailyTarget = remoteConfig.getString(
            Constants.RemoteConfigKeys.DAILT_TARGET_TICKET,
            Constants.RemoteConfigDefaults.DEFAULT_TARGET_TICKET
        )

        val targets = Json.decodeFromString<List<StepTicketBucket>>(dailyTarget)

        Napier.v { "dailyTarget=${dailyTarget}, targets=${targets}" }
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    suspend fun mayCreateUserDoc2() {
        val user = _uiState.value.user
        if (user === null) return
        if (!isAuthorized) return

        val isUserExist = Firebase.firestore.document("users/${user.email}").get().exists
        if (!isUserExist) {
            firestoreService.create<FirestoreUserModel2>("users", user.email, data = mapOf("count" to 0))
        } else {
            firestoreService.update("users", user.email, data = mapOf("count" to 0))
        }

        val utcNowString = Clock.System.now()
            .toLocalDateTime(TimeZone.UTC)
            .format(LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd") })

        val isDailyStepCountExist = Firebase.firestore.document("dailyStepCounts/${user.email}_${utcNowString}").get().exists
        val dailyStepCountsMap = mapOf(
            "user" to user.email,
            "date" to utcNowString,
            "count" to getSummedStepsCount(isAuthorized = isAuthorized)
        )

        if (!isDailyStepCountExist) {
            firestoreService.create<FirestoreUserModel2>(
                collection = "dailyStepCounts",
                id = "${user.email}_${utcNowString}",
                data = dailyStepCountsMap
            )
        } else {
            firestoreService.update(
                collection = "dailyStepCounts",
                id = "${user.email}_${utcNowString}",
                data = dailyStepCountsMap
            )
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

    private suspend fun getSummedStepsCountListForLastDays(daysDuration: Duration, isAuthorized: Boolean): Map<String, Int>? {
        if (!isAuthorized) {
            return null
        }

        val linkedHashMap = LinkedHashMap<String, Int>()
        for (i in 0..daysDuration.inWholeDays) {
            Napier.v(tag = "getSummedStepsCountListForLastDays", message = "i=$i")
            val now = Clock.System.now().minus(i.days).toLocalDateTime(TimeZone.currentSystemDefault())
            val todaysDate = LocalDate(now.year, now.monthNumber, now.dayOfMonth)
            val start = todaysDate.atStartOfDayIn(TimeZone.currentSystemDefault())
            val end = todaysDate.atTime(hour = 23, minute = 59, second = 59, nanosecond = 999999999)
                .toInstant(TimeZone.currentSystemDefault())
            val dateString = "${now.year}${now.monthNumber}${now.dayOfMonth}"

            val stepList = healthManager.readSteps(start, end)
            val stepCounts = stepList.getOrDefault(emptyList())

            linkedHashMap[dateString] = stepCounts.sumOf { it.count }
        }

        return linkedHashMap
    }

    private suspend fun getSummedStepsCount(daysDuration: Duration? = null, isAuthorized: Boolean): Int {
        if (!isAuthorized) return 0
        val clockNow = Clock.System.now()
        if (daysDuration != null) {
            clockNow.minus(daysDuration)
        }
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val todaysDate = LocalDate(now.year, now.monthNumber, now.dayOfMonth)
        val start = todaysDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val end = todaysDate.atTime(hour = 23, minute = 59, second = 59, nanosecond = 999999999)
            .toInstant(TimeZone.currentSystemDefault())
        val stepList = healthManager.readSteps(start, end)
        val stepCounts = stepList.getOrDefault(emptyList())

        return stepCounts.sumOf { it.count }
    }

    /**
     * To create user doc on Firebase if not exists
     */
    private suspend fun mayCreateUserCollection() {
        repository.createOrUpdateUserCollection(user, isAuthorized)
    }
}