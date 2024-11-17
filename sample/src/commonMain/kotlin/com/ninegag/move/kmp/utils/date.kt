package com.ninegag.move.kmp.utils

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