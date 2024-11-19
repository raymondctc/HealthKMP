import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.ninegag.move.kmp.MainViewModel
import com.ninegag.move.kmp.ui.challenge.ChallengePeriodHeader
import com.ninegag.move.kmp.ui.challenge.TargetHeader
import com.ninegag.move.kmp.ui.user.UserRow
import kotlinx.coroutines.launch

@Composable
fun MainPage(viewModel: MainViewModel, paddingValues: androidx.compose.foundation.layout.PaddingValues) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UserRow(user = uiState.user!!)
            if (!uiState.isHealthManagerAvailable) {
                Text("Sorry, this application is not supported on your device")
            } else if (!uiState.isAuthorized) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.requestAuthorization()
                        }
                    }
                ) {
                    Text("Authorize Health's access")
                }
            }
        }
        item {
            ChallengePeriodHeader(challengePeriod = uiState.challengePeriod)
        }
        item {
            TargetHeader(
                currentTarget = 6000,
                currentRewards = 1,
                currentProgress = uiState.currentDaySteps,
                currentReward = 1)
        }
    }
}