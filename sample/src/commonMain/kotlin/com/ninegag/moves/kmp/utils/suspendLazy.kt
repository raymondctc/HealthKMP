package com.ninegag.moves.kmp.utils

class SuspendLazy<T>(private val initializer: suspend () -> T) {
    private var value: T? = null

    suspend fun getValue(): T {
        if (value == null) {
            value = initializer()
        }
        return value!!
    }
}

// Extension function to simplify usage
fun <T> suspendLazy(initializer: suspend () -> T) = SuspendLazy(initializer)