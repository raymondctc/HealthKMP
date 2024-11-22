package com.ninegag.moves.kmp.utils

/**
 * Don't want to include a library for this at the moment, seems no good lib to use
 */
fun Int.toThousandSeparatedString(): String {
    val numberString = toString()
    val result = StringBuilder()
    var count = 0

    for (i in numberString.length - 1 downTo 0) {
        result.append(numberString[i])
        count++

        // Add a comma after every 3 digits, except for the last group
        if (count % 3 == 0 && i != 0) {
            result.append(',')
        }
    }
    return result.reverse().toString()
}