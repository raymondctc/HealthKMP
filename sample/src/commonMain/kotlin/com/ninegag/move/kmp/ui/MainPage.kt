import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninegag.move.kmp.MainViewModel
import com.ninegag.move.kmp.ui.challenge.ChallengePeriodHeader
import com.ninegag.move.kmp.ui.challenge.TargetHeader

@Composable
fun MainPage(viewModel: MainViewModel, paddingValues: androidx.compose.foundation.layout.PaddingValues) {
    val uiState by viewModel.uiState.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UserRow(user = uiState.user!!)
        }
        item {
            ChallengePeriodHeader(challengePeriod = uiState.challengePeriod)
        }
        item {
            TargetHeader(
                currentTarget = 6000,
                currentRewards = 1,
                currentProgress = 5000,
                currentReward = 1)
        }
        uiState.stepsRecord.forEach { (date, count) ->
            item {
                Text(text = "Date=$date, steps count= $count")
            }
        }
    }
}