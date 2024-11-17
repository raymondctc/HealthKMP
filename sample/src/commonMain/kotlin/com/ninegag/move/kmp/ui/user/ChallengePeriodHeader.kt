package com.ninegag.move.kmp.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninegag.move.kmp.model.ChallengePeriod

@Composable
fun ChallengePeriodHeader(
    challengePeriod: ChallengePeriod
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Challenge period", style = MaterialTheme.typography.h5)
        Text("${challengePeriod.start} to ${challengePeriod.end}", style = MaterialTheme.typography.h6)
    }
}