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
import com.ninegag.moves.kmp.model.StepsAndTicketsRecord
import com.ninegag.moves.kmp.model.firestore.FirestoreMonthlyRank
import com.ninegag.moves.kmp.utils.fromYYYYMMDDToMonthString
import com.ninegag.moves.kmp.utils.suspendLazy
import com.ninegag.moves.kmp.utils.toDailyStepsDateString
import com.tweener.firebase.remoteconfig.datasource.RemoteConfigDataSource
import com.vitoksmile.kmp.health.records.StepsRecord
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import kotlin.random.Random

typealias TicketsAndNextTarget = Pair<Int, Int>

class MoveAppRepository : KoinComponent {

    private val remoteConfigService: RemoteConfigDataSource by inject()
    private val firestoreService: FirestoreService by inject()
    private val healthManager: HealthManager by inject()
    private val tz = TimeZone.currentSystemDefault()

    val targetStepsList = suspendLazy {
        val dailyTarget = remoteConfigService.getString(
            Constants.RemoteConfigKeys.DAILT_TARGET_TICKET,
            Constants.RemoteConfigDefaults.DEFAULT_TARGET_TICKET
        )
        Json.decodeFromString<List<StepTicketBucket>>(dailyTarget)
    }

    suspend fun getTicketsEarnedForSteps(steps: Int): TicketsAndNextTarget {
        val targets = targetStepsList.getValue()
        val bucket = targets.find { steps in it.stepsMin..it.stepsMax }
        val currentReward = bucket?.tickets ?: 0
        val nextTarget = targets.indexOfFirst { steps < it.stepsMin }.let { index ->
            if (index != -1) targets[index].stepsMin else bucket?.stepsMin ?: 0
        }
        return Pair(currentReward, nextTarget)
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

    private fun getToday(): LocalDateTime {
        return Clock.System.todayIn(tz).atStartOfDayIn(tz).toLocalDateTime(tz)
    }

    suspend fun getStepsForToday(): Int {
        val today = getToday()
        val start = LocalDate(today.year, today.monthNumber, today.dayOfMonth).atStartOfDayIn(tz)
        val steps = healthManager.readSteps(
            startTime = start,
            endTime = today.toInstant(tz)
        )

        return steps.getOrDefault(emptyList()).sumOf { it.count }
    }

    suspend fun getStepsFromStartOfMonthToToday(): Map<String, StepsAndTicketsRecord> {
        val linkedHashMap = LinkedHashMap<String, StepsAndTicketsRecord>()
        val localDateTime = getToday()
        val startOfMonth = LocalDate(localDateTime.year, localDateTime.monthNumber, 1).atStartOfDayIn(tz)
        val diff = localDateTime.toInstant(tz).minus(startOfMonth)
        val daysDuration = diff.inWholeDays.days

        for (i in 0 .. daysDuration.inWholeDays) {
            val curr = startOfMonth.plus(i.days).toLocalDateTime(tz)
            val date = LocalDate(curr.year, curr.monthNumber, curr.dayOfMonth)

            val start = date.atStartOfDayIn(tz)
            val end = date.atTime(hour = 23, minute = 59, second = 59, nanosecond = 999999999)
                .toInstant(tz)
            val dateString = curr.toDailyStepsDateString()

            val stepList = healthManager.readSteps(start, end)
            val stepCountsOfCurrDate = stepList.getOrDefault(emptyList())

            val steps = stepCountsOfCurrDate.sumOf { it.count }
            linkedHashMap[dateString] = StepsAndTicketsRecord(
                steps = steps,
                tickets = getTicketsEarnedForSteps(steps).first
            )
        }

        return linkedHashMap
    }

    suspend fun getPrevMonthSteps(): Map<String, StepsAndTicketsRecord> {
        val linkedHashMap = LinkedHashMap<String, StepsAndTicketsRecord>()

        val today = Clock.System.todayIn(tz)
        val previousMonth = today.minus(DatePeriod(months = 1))
        val startOfPrevMonth = LocalDate(previousMonth.year, previousMonth.monthNumber, 1).atStartOfDayIn(tz)
        val startOfMonth = LocalDate(today.year, today.monthNumber, 1).atStartOfDayIn(tz)

        val diff = startOfMonth.minus(startOfPrevMonth)
        val daysDuration = diff.inWholeDays.days

        for (i in 0 ..< daysDuration.inWholeDays) {
            val curr = startOfPrevMonth.plus(i.days).toLocalDateTime(tz)
            val date = LocalDate(curr.year, curr.monthNumber, curr.dayOfMonth)

            val start = date.atStartOfDayIn(tz)
            val end = date.atTime(hour = 23, minute = 59, second = 59, nanosecond = 999999999)
                .toInstant(tz)
            val dateString = curr.toDailyStepsDateString()

            val stepList = healthManager.readSteps(start, end)
            val stepCountsOfCurrDate = stepList.getOrDefault(emptyList())

            val steps = stepCountsOfCurrDate.sumOf { it.count }
            linkedHashMap[dateString] = StepsAndTicketsRecord(
                steps = steps,
                tickets = getTicketsEarnedForSteps(steps).first
            )

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
        stepsMap: Map<String, StepsAndTicketsRecord>
    ) {
        var monthToDateSteps = 0
        var monthToDateTickets = 0
        for ((key, value) in stepsMap) {
            val steps = value.steps
            val tickets = value.tickets
            val data = mapOf(
                Firestore.CollectionFields.USERNAME to user.name,
                Firestore.CollectionFields.EMAIL to user.email,
                Firestore.CollectionFields.AVATAR_URL to user.avatarUrl,
                Firestore.CollectionFields.STEPS to steps,
                Firestore.CollectionFields.TICKETS to tickets
            )
            createOrUpdateCollection<FirestoreDailyRank>(
                collection = Firestore.Collections.STEPS,
                documentId = "${Firestore.Collections.DAILY_STEPS}/$key/${user.email}",
                data = data
            )

            monthToDateSteps += steps
            monthToDateTickets += tickets
        }

        val firstRecordKey = stepsMap.keys.first()
        val monthString = firstRecordKey.fromYYYYMMDDToMonthString()
        val data = mapOf(
            Firestore.CollectionFields.USERNAME to user.name,
            Firestore.CollectionFields.EMAIL to user.email,
            Firestore.CollectionFields.AVATAR_URL to user.avatarUrl,
            Firestore.CollectionFields.STEPS to monthToDateSteps,
            Firestore.CollectionFields.TICKETS to monthToDateTickets
        )

        createOrUpdateCollection<FirestoreMonthlyRank>(
            collection = Firestore.Collections.STEPS,
            documentId = "${Firestore.Collections.MONTHLY_STEPS}/$monthString/${user.email}",
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
            firestoreService.get<T>(collection, documentId)
            firestoreService.update(collection, documentId, data)
        } catch (e: Exception) {
            Napier.v(tag = "FirestoreOp", message = "Collection=${collection}, documentId=${documentId} doesn't exist, now creating collection")
            firestoreService.create<T>(collection, documentId, data)
        }
    }

}