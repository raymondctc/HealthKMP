package com.ninegag.moves.kmp.repository

import com.ninegag.moves.kmp.model.User
import com.ninegag.moves.kmp.model.firestore.FirestoreDailyRank
import com.ninegag.moves.kmp.model.firestore.FirestoreUser
import com.tweener.firebase.firestore.FirestoreService
import com.tweener.firebase.firestore.model.FirestoreModel
import com.tweener.firebase.auth.provider.google.FirebaseGoogleAuthProvider
import com.tweener.firebase.remoteconfig.datasource.RemoteConfigDataSource
import com.vitoksmile.kmp.health.HealthManager
import com.vitoksmile.kmp.health.readSteps
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.days
import com.ninegag.moves.kmp.Constants.Firestore
import com.vitoksmile.kmp.health.HealthDataType

class MoveAppRepository : KoinComponent {

    private val remoteConfig: RemoteConfigDataSource by inject()
    private val firestoreService: FirestoreService by inject()
    private val healthManager: HealthManager by inject()

    suspend fun createOrUpdateUserCollection(
        user: User?,
        isAuthorized: Boolean
    ) {
        if (user === null || !isAuthorized) {
            Napier.v(tag = "FirestoreOp", message = "User is ${user}, isAuthorized=$isAuthorized, stop creating collection")
            return
        }

        val data = mapOf(
            Firestore.CollectionFields.USERNAME to user.name,
            Firestore.CollectionFields.EMAIL to user.email,
            Firestore.CollectionFields.AVATAR_URL to user.avatarUrl
        )
        createOrUpdateCollection<FirestoreUser>(
            collection = Firestore.Collections.USER,
            documentId = user.email,
            data = data
        )
    }

    suspend fun createOrUpdateStepsCollection(
        user: User
    ) {
        val linkedHashMap = LinkedHashMap<String, Int>()
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val startOfMonth = LocalDate(localDateTime.year, localDateTime.monthNumber, 1).atStartOfDayIn(TimeZone.currentSystemDefault())
        val diff = localDateTime.toInstant(TimeZone.currentSystemDefault()).minus(startOfMonth)
        val daysDuration = diff.inWholeDays.days

        for (i in 0 .. daysDuration.inWholeDays) {
            val curr = startOfMonth.plus(i.days).toLocalDateTime(TimeZone.currentSystemDefault())
            val date = LocalDate(curr.year, curr.monthNumber, curr.dayOfMonth)

            val start = date.atStartOfDayIn(TimeZone.currentSystemDefault())
            val end = date.atTime(hour = 23, minute = 59, second = 59, nanosecond = 999999999)
                .toInstant(TimeZone.currentSystemDefault())
            val dateString = "${curr.year}-${curr.monthNumber.toString().padStart(2, '0')}-${curr.dayOfMonth.toString().padStart(2, '0')}"

            val stepList = healthManager.readSteps(start, end)
            val stepCountsOfCurrDate = stepList.getOrDefault(emptyList())

            linkedHashMap[dateString] = stepCountsOfCurrDate.sumOf { it.count }

            val data = mapOf(
                Firestore.CollectionFields.USERNAME to user.name,
                Firestore.CollectionFields.EMAIL to user.email,
                Firestore.CollectionFields.AVATAR_URL to user.avatarUrl,
                Firestore.CollectionFields.STEPS to linkedHashMap[dateString]
            )

            createOrUpdateCollection<FirestoreDailyRank>(
                collection = Firestore.Collections.STEPS,
                documentId = "${user.email}/${Firestore.Collections.DAILY_STEPS}/$dateString",
                data = data
            )
        }

        val monthString = "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}"
        val data = mapOf(
            Firestore.CollectionFields.USERNAME to user.name,
            Firestore.CollectionFields.EMAIL to user.email,
            Firestore.CollectionFields.AVATAR_URL to user.avatarUrl,
            Firestore.CollectionFields.STEPS to linkedHashMap.entries.sumOf { it.value }
        )

        createOrUpdateCollection<FirestoreDailyRank>(
            collection = "steps",
            documentId = "${user.email}/${Firestore.Collections.MONTHLY_STEPS}/$monthString",
            data = data
        )
    }

    /**
     * TODO: ensure return data is correct w/ writeTypes
     */
    suspend fun getTodaySteps(): Int {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val start = today.atStartOfDayIn(TimeZone.currentSystemDefault())
        val end = today.atTime(23, 59, 59).toInstant(TimeZone.currentSystemDefault())

        val stepList = healthManager.readSteps(start, end)
        val stepCountsOfToday = stepList.getOrDefault(emptyList())
        println("HealthManager.readData result: $stepList")
        return stepCountsOfToday.sumOf { it.count }
    }

    suspend fun getMinStepTarget(): Int {
        // Retrieve the value from Remote Config
        val minStepTarget = remoteConfig.getLong(
            "min_step_target",
            defaultValue =6000
        )
        return minStepTarget.toInt()
    }

    // TODO: admin updates this
    suspend fun updateMinStepTarget(newTarget: Int) {
    }

    private suspend inline fun <reified T: FirestoreModel> createOrUpdateCollection(
        collection: String,
        documentId: String,
        data: Map<String, Any?>
    ) {
        try {
            // TODO: Library doesn't allow to return a nullable user to check existence, doing this a try-catch
            firestoreService.get<FirestoreDailyRank>(collection, documentId)
            firestoreService.update(collection, documentId, data)
        } catch (e: Exception) {
            Napier.v(tag = "FirestoreOp", message = "Collection=${collection}, documentId=${documentId} doesn't exist, now creating collection")
            firestoreService.create<T>(collection, documentId, data)
        }
    }

}