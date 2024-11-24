package com.ninegag.moves.kmp.repository

import com.ninegag.moves.kmp.Constants
import com.ninegag.moves.kmp.model.User
import com.ninegag.moves.kmp.model.firestore.FirestoreDailyRank
import com.ninegag.moves.kmp.model.firestore.FirestoreUser
import com.tweener.firebase.firestore.FirestoreService
import com.tweener.firebase.firestore.model.FirestoreModel
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
import com.ninegag.moves.kmp.model.StepTicketBucket
import com.ninegag.moves.kmp.model.firestore.FirestoreMonthlyRank
import com.ninegag.moves.kmp.utils.toDailyStepsDateString
import com.ninegag.moves.kmp.utils.toMonthlyStepsDateString
import com.tweener.firebase.remoteconfig.datasource.RemoteConfigDataSource
import com.vitoksmile.kmp.health.records.StepsRecord
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.random.Random

class MoveAppRepository : KoinComponent {

    private val remoteConfigService: RemoteConfigDataSource by inject()
    private val firestoreService: FirestoreService by inject()
    private val healthManager: HealthManager by inject()

    suspend fun getTargetConfigs(): List<StepTicketBucket> {
        val dailyTarget = remoteConfigService.getString(
            Constants.RemoteConfigKeys.DAILT_TARGET_TICKET,
            Constants.RemoteConfigDefaults.DEFAULT_TARGET_TICKET
        )
        return Json.decodeFromString<List<StepTicketBucket>>(dailyTarget)
    }

    /**
     * To update user's profile on Firebase
     */
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

    suspend fun getStepsFromStartOfMonthToToday(): Map<String, Int> {
        val linkedHashMap = LinkedHashMap<String, Int>()
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val localDateTime = now.toLocalDateTime(tz)
        val startOfMonth = LocalDate(localDateTime.year, localDateTime.monthNumber, 1).atStartOfDayIn(TimeZone.currentSystemDefault())
        val diff = localDateTime.toInstant(TimeZone.currentSystemDefault()).minus(startOfMonth)
        val daysDuration = diff.inWholeDays.days

        for (i in 0 .. daysDuration.inWholeDays + 1) {
            val curr = startOfMonth.plus(i.days).toLocalDateTime(tz)
            val date = LocalDate(curr.year, curr.monthNumber, curr.dayOfMonth)

            val start = date.atStartOfDayIn(TimeZone.currentSystemDefault())
            val end = date.atTime(hour = 23, minute = 59, second = 59, nanosecond = 999999999)
                .toInstant(TimeZone.currentSystemDefault())
            val dateString = curr.toDailyStepsDateString()

            val stepList = healthManager.readSteps(start, end)
            val stepCountsOfCurrDate = stepList.getOrDefault(emptyList())

            linkedHashMap[dateString] = stepCountsOfCurrDate.sumOf { it.count }
        }

        return linkedHashMap
    }

    /**
     * To update user's steps on Firebase from start of the month to today
     *
     * @param user User object
     * @param stepsMap Map of date string to step count
     */
    suspend fun createOrUpdateStepsCollection(
        user: User,
        stepsMap: Map<String, Int>
    ) {
        var monthToDateSteps = 0
        for ((key, value) in stepsMap) {
            val data = mapOf(
                Firestore.CollectionFields.USERNAME to user.name,
                Firestore.CollectionFields.EMAIL to user.email,
                Firestore.CollectionFields.AVATAR_URL to user.avatarUrl,
                Firestore.CollectionFields.STEPS to value
            )
            createOrUpdateCollection<FirestoreDailyRank>(
                collection = Firestore.Collections.STEPS,
                documentId = "${user.email}/${Firestore.Collections.DAILY_STEPS}/$key",
                data = data
            )

            monthToDateSteps += value
        }

        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val monthString = localDateTime.toMonthlyStepsDateString()
        val data = mapOf(
            Firestore.CollectionFields.USERNAME to user.name,
            Firestore.CollectionFields.EMAIL to user.email,
            Firestore.CollectionFields.AVATAR_URL to user.avatarUrl,
            Firestore.CollectionFields.STEPS to monthToDateSteps
        )

        createOrUpdateCollection<FirestoreMonthlyRank>(
            collection = Firestore.Collections.STEPS,
            documentId = "${user.email}/${Firestore.Collections.MONTHLY_STEPS}/$monthString",
            data = data
        )
    }


    // For testing purpose
    suspend fun writeRandomSteps(
        start: Instant,
        end: Instant,
    ) {
        val steps1 = Random.nextInt(10000)
        val records = listOf(
            StepsRecord(
                startTime = start,
                endTime = end,
                count = steps1
            ),
        )

        healthManager.writeData(records)

        Napier.v { "writeRandomSteps, steps1=$steps1, \n" }
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