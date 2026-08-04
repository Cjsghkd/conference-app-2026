import androidx.compose.material3.Card
import androidx.compose.material3.Column
import androidx.compose.material3.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TooDeep(titles: List<String>) {
    Scaffold { padding ->
        Column {
            LazyColumn {
                items(titles) { title ->
                    <!COMPOSABLE_NESTING_TOO_DEEP!>Card<!> {
                        Text(title)
                    }
                }
            }
        }
    }
}

@Composable
fun WithinLimit(titles: List<String>) {
    Scaffold { padding ->
        Column {
            LazyColumn {
                items(titles) { title ->
                    Text(title)
                }
            }
        }
    }
}
