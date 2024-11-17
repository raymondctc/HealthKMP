package com.ninegag.move.kmp.ui.challenge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
        Text("Challenge period", style = MaterialTheme.typography.headlineSmall)
        Text("${challengePeriod.start} to ${challengePeriod.end}", style = MaterialTheme.typography.headlineMedium)
    }
}