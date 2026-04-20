package edu.wcupa.csc461.rankit

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import db
import edu.wcupa.csc461.rankit.ui.theme.RankitTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

//test
// 1. Data Models
data class PollOption(
    val id: Int,
    val firestoreId: String = "",
    val name: String,
    val votes: Int
)

data class Poll(
    val id: Int,
    val firestoreId: String = "",
    val title: String,
    val category: String,
    val options: List<PollOption>
)

// 2. ViewModel - Managing State and Business Logic
class PollViewModel(application: Application) : AndroidViewModel(application) {
    private val database = db.getInstance(application)

    private val defaultCategories = listOf("All", "Food", "Sports", "Gaming", "Movies")

    private val _categories = MutableStateFlow(defaultCategories)
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private var nextId = 1

    private val _polls = MutableStateFlow(emptyList<Poll>())
    val polls: StateFlow<List<Poll>> = _polls.asStateFlow()

    init {
        loadFromDb()
    }

    private fun loadFromDb() {
        // Load user-added filter categories, merge with defaults
        database.loadFilterCategories { dbCategories ->
            if (dbCategories.isNotEmpty()) {
                val merged = defaultCategories + dbCategories.filter { it !in defaultCategories }
                _categories.value = merged
            }
        }

        // Load polls with their options from Firestore
        database.loadPolls { dbPolls ->
            if (dbPolls.isNotEmpty()) {
                val polls = dbPolls.map { dbPoll ->
                    Poll(
                        id = nextId++,
                        firestoreId = dbPoll.firestoreId,
                        title = dbPoll.title,
                        category = dbPoll.filterCategory,
                        options = dbPoll.options.map { opt ->
                            PollOption(
                                id = nextId++,
                                firestoreId = opt.firestoreId,
                                name = opt.name,
                                votes = opt.score
                            )
                        }
                    )
                }
                _polls.value = polls
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty() && !_categories.value.contains(trimmed)) {
            _categories.update { it + trimmed }
            database.createFilterCategory(trimmed)
        }
    }

    fun addPoll(title: String, category: String, optionNames: List<String>) {
        val localId = nextId++
        val options = optionNames.map { name -> PollOption(
            id = nextId++,
            firestoreId = "",
            name = name,
            votes = 0
        ) }
        val poll = Poll(localId, "", title.trim(), category, options)
        _polls.update { it + poll }

        database.createCategory(title.trim(), category) { firestoreId ->
            _polls.update { polls ->
                polls.map { p -> if (p.id == localId) p.copy(firestoreId = firestoreId) else p }
            }
            optionNames.forEach { optionName -> database.addOption(firestoreId, optionName) }
        }
    }

    fun addOption(pollId: Int, optionName: String) {
        val trimmed = optionName.trim()
        if (trimmed.isEmpty()) return
        _polls.update { currentPolls ->
            currentPolls.map { poll ->
                if (poll.id == pollId) {
                    poll.copy(options = poll.options + PollOption(
                        nextId++, trimmed, "0",
                        votes = 0
                    ))
                } else poll
            }
        }

        val firestoreId = _polls.value.find { it.id == pollId }?.firestoreId
        if (!firestoreId.isNullOrEmpty()) {
            database.addOption(firestoreId, trimmed)
        }
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
        val firestoreId = _polls.value
            .find { it.id == pollId }
            ?.firestoreId

        val optionFirestoreId = _polls.value
            .find { it.id == pollId }
            ?.options
            ?.find { it.id == optionId }
            ?.firestoreId

        if (firestoreId != null && optionFirestoreId != null) {
            database.vote(firestoreId, optionFirestoreId)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RankItApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankItApp() {
    RankitTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Rank.it",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    navigationIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_rankit_logo),
                            contentDescription = "Rank.it logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(40.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            PollScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

fun categoryEmoji(category: String): String = when (category.lowercase()) {
    "all"        -> "🌐"
    "food"       -> "🍔"
    "sports"     -> "⚽"
    "gaming"     -> "🎮"
    "movies"     -> "🎬"
    "music"      -> "🎵"
    "travel"     -> "✈️"
    "tech"       -> "💻"
    "science"    -> "🔬"
    "art"        -> "🎨"
    "fashion"    -> "👗"
    "health"     -> "💪"
    "animals"    -> "🐾"
    "politics"   -> "🗳️"
    "education"  -> "📚"
    "finance"    -> "💰"
    "nature"     -> "🌿"
    "news"       -> "📰"
    else         -> "📌"
}

@Composable
fun PollScreen(
    modifier: Modifier = Modifier,
    viewModel: PollViewModel = viewModel()
) {
    val allPolls by viewModel.polls.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddPollDialog by remember { mutableStateOf(false) }
    var addOptionPollId by remember { mutableStateOf<Int?>(null) }

    val filteredPolls = if (selectedCategory == "All") {
        allPolls
    } else {
        allPolls.filter { it.category == selectedCategory }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category row with "+" button at the end
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text("${categoryEmoji(category)} $category") }
                    )
                }
                item {
                    Button(
                        onClick = { showAddCategoryDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("+ Category")
                    }
                }
            }


            // List of polls
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredPolls, key = { it.id }) { poll ->
                    PollCard(
                        poll = poll,
                        onUpvote = { optionId -> viewModel.upvote(poll.id, optionId) },
                        onDownvote = { optionId -> viewModel.downvote(poll.id, optionId) },
                        onAddOption = { addOptionPollId = poll.id }
                    )
                }
            }
        }

        // FAB to add a new poll
        FloatingActionButton(
            onClick = { showAddPollDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name ->
                viewModel.addCategory(name)
                showAddCategoryDialog = false
            }
        )
    }

    if (showAddPollDialog) {
        AddPollDialog(
            categories = categories.filter { it != "All" },
            onDismiss = { showAddPollDialog = false },
            onConfirm = { title, category, options ->
                viewModel.addPoll(title, category, options)
                showAddPollDialog = false
            }
        )
    }

    addOptionPollId?.let { pollId ->
        AddOptionDialog(
            onDismiss = { addOptionPollId = null },
            onConfirm = { name ->
                viewModel.addOption(pollId, name)
                addOptionPollId = null
            }
        )
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPollDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val options = remember { mutableStateListOf("", "") }

    val validOptions = options.filter { it.isNotBlank() }
    val canSubmit = title.isNotBlank() && selectedCategory.isNotEmpty() && validOptions.size >= 2

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Poll") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Poll title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                if (categories.isEmpty()) {
                    Text(
                        text = "Add a category first using the + Category button.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Options (min. 2)", style = MaterialTheme.typography.labelLarge)

                options.forEachIndexed { idx, opt ->
                    OutlinedTextField(
                        value = opt,
                        onValueChange = { options[idx] = it },
                        label = { Text("Option ${idx + 1}") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                TextButton(onClick = { options.add("") }) {
                    Text("+ Add Option")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (canSubmit) onConfirm(title, selectedCategory, validOptions) },
                enabled = canSubmit
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddOptionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Option") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Option name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PollCard(
    poll: Poll,
    onUpvote: (Int) -> Unit,
    onDownvote: (Int) -> Unit,
    onAddOption: () -> Unit
) {
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
                text = "${categoryEmoji(poll.category)} ${poll.category}",
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

            TextButton(
                onClick = onAddOption,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("+ Add Option")
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
