package edu.wcupa.csc461.rankit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import db
import edu.wcupa.csc461.rankit.ui.theme.RankitTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 1. Data Models
data class PollOption(
    val id: Int,
    val name: String,
    val votes: Int
)

data class Poll(
    val id: Int,
    val title: String,
    val category: String,
    val options: List<PollOption>
)

// 2. ViewModel - Managing State and Business Logic
class PollViewModel : ViewModel() {
    // List of categories for filtering
    val categories = listOf("All", "Food", "Sports", "Gaming", "Movies")

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // UI State held in StateFlow - now with multiple polls
    private val _polls = MutableStateFlow(
        listOf(
            Poll(
                id = 1,
                title = "Best Fruits",
                category = "Food",
                options = listOf(
                    PollOption(1, "Apple", 0),
                    PollOption(2, "Banana", 0),
                    PollOption(3, "Orange", 0),
                    PollOption(4, "Grape", 0)
                )
            ),
            Poll(
                id = 2,
                title = "Favorite Sport",
                category = "Sports",
                options = listOf(
                    PollOption(5, "Soccer", 0),
                    PollOption(6, "Basketball", 0),
                    PollOption(7, "Tennis", 0)
                )
            ),
            Poll(
                id = 3,
                title = "Top Gaming Consoles",
                category = "Gaming",
                options = listOf(
                    PollOption(8, "PS5", 0),
                    PollOption(9, "Xbox Series X", 0),
                    PollOption(10, "Nintendo Switch", 0)
                )
            )
        )
    )
    val polls: StateFlow<List<Poll>> = _polls.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // Business Logic - Upvote
    fun upvote(pollId: Int, optionId: Int) {
        _polls.update { currentPolls ->
            currentPolls.map { poll ->
                if (poll.id == pollId) {
                    poll.copy(
                        options = poll.options.map { option ->
                            if (option.id == optionId) option.copy(votes = option.votes + 1) else option
                        }
                    )
                } else poll
            }
        }
    }

    // Business Logic - Downvote
    fun downvote(pollId: Int, optionId: Int) {
        _polls.update { currentPolls ->
            currentPolls.map { poll ->
                if (poll.id == pollId) {
                    poll.copy(
                        options = poll.options.map { option ->
                            if (option.id == optionId) option.copy(votes = (option.votes - 1).coerceAtLeast(0)) else option
                        }
                    )
                } else poll
            }
        }
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var db: db

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RankitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PollScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PollScreen(
    modifier: Modifier = Modifier,
    viewModel: PollViewModel = viewModel()
) {
    val allPolls by viewModel.polls.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    // Filter polls based on selected category
    val filteredPolls = if (selectedCategory == "All") {
        allPolls
    } else {
        allPolls.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Category Selection UI
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category) }
                )
            }
        }

        // List of polls
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredPolls, key = { it.id }) { poll ->
                PollCard(
                    poll = poll,
                    onUpvote = { optionId -> viewModel.upvote(poll.id, optionId) },
                    onDownvote = { optionId -> viewModel.downvote(poll.id, optionId) }
                )
            }
        }
    }
}

@Composable
fun PollCard(
    poll: Poll,
    onUpvote: (Int) -> Unit,
    onDownvote: (Int) -> Unit
) {
    // Dynamic reordering based on votes descending
    val sortedOptions = poll.options.sortedByDescending { it.votes }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = poll.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = poll.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            sortedOptions.forEach { option ->
                PollOptionRow(
                    option = option,
                    onUpvote = { onUpvote(option.id) },
                    onDownvote = { onDownvote(option.id) }
                )
            }
        }
    }
}

@Composable
fun PollOptionRow(
    option: PollOption,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = option.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "${option.votes}",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = onUpvote,
            modifier = Modifier.padding(end = 4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("▲")
        }

        Button(
            onClick = onDownvote,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("▼")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PollScreenPreview() {
    RankitTheme {
        PollScreen()
    }
}
