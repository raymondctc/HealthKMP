package com.ninegag.move.kmp.ui.challenge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TargetHeader(
    currentTarget: Int,
    currentRewards: Int,
    currentProgress: Int,
    currentReward: Int
) {
    // Target: 6000 steps
    // Rewards: 1 ticket
    // Progress: N steps (N%)
    // Tickets earned: M
    Card(
        modifier = Modifier.fillMaxWidth(),

    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null)
                Text("Today's progress", style = MaterialTheme.typography.titleLarge)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Steps:", style = MaterialTheme.typography.labelLarge)
                Text("$currentProgress / $currentTarget", style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ticket(s) earned:", style = MaterialTheme.typography.labelLarge)
                Text("$currentReward", style = MaterialTheme.typography.bodyLarge)
            }
        }

    }
}