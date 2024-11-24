package com.ninegag.moves.kmp.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month

// Extension function to calculate days in a month
fun Month.numberOfDays(year: Int): Int {
    val leapYear = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    return when (this) {
        Month.FEBRUARY -> if (leapYear) 29 else 28
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        else -> 31
    }
}

fun LocalDateTime.toDailyStepsDateString(): String {
    val dateString = "${year}-${
        monthNumber.toString().padStart(2, '0')
    }-${dayOfMonth.toString().padStart(2, '0')}"

    return dateString
}

fun LocalDateTime.toMonthlyStepsDateString(): String {
    return "${year}-${monthNumber.toString().padStart(2, '0')}"
}