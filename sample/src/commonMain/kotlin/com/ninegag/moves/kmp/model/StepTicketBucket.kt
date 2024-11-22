package com.ninegag.moves.kmp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StepTicketBucket(
    @SerialName("steps_min") val stepsMin: Int,
    @SerialName("steps_max") val stepsMax: Int,
    val tickets: Int
)

data class StepTicketBucketUiValues(
    val stepsMin: String,
    val stepsMax: String,
    val tickets: String
)