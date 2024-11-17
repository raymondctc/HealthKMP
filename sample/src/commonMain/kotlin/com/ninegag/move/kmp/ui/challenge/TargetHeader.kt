package com.ninegag.move.kmp.ui.challenge

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
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null)
            Text("Today's progress", style = MaterialTheme.typography.titleMedium)
        }
    }
}