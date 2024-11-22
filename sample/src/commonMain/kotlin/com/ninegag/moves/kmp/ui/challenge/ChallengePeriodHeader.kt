package com.ninegag.moves.kmp.ui.challenge

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninegag.moves.kmp.model.ChallengePeriod
import com.ninegag.moves.kmp.model.StepTicketBucketUiValues

@Composable
fun ChallengePeriodHeader(
    challengePeriod: ChallengePeriod,
    stepTicketBucket: List<StepTicketBucketUiValues>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Text("Challenge period", style = MaterialTheme.typography.titleLarge)
            }
            Text("${challengePeriod.start} to ${challengePeriod.end}", style = MaterialTheme.typography.bodyLarge)
            RewardsDetails(stepTicketBucket)
        }

    }
}

@Composable
fun RewardsDetails(stepTicketBucket: List<StepTicketBucketUiValues>) {
    var expanded by remember { mutableStateOf (false) }

    Column(
        modifier = Modifier.fillMaxWidth().clickable {
            expanded = !expanded
        },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Rewards",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            if (expanded) Icon(Icons.Default.ArrowDropUp, contentDescription = null) else Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        if (expanded) {
            stepTicketBucket.mapIndexed { index, it ->
                if (index == stepTicketBucket.size - 1) {
                    Text(
                        text = ">= ${it.stepsMin} steps: ${it.tickets} tickets",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "${it.stepsMin} - ${it.stepsMax} steps: ${it.tickets} tickets",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            }
        }
    }
}