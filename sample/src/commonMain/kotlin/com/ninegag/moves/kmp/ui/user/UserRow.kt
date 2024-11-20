

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninegag.moves.kmp.model.User

@Composable
fun UserRow(user: User) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Avatar(user.avatarUrl)
        Column(
            modifier = Modifier.padding(start = 8.dp),
        ) {
            Text("Welcome, ${user.name}")
        }
    }
}

@Composable
fun Avatar(url: String) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier.clip(CircleShape).size(48.dp)
    )
}