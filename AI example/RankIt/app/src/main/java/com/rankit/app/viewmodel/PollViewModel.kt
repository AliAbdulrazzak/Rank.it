package com.rankit.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rankit.app.data.model.Poll
import com.rankit.app.data.model.PollCategory
import com.rankit.app.data.model.PollOption
import com.rankit.app.data.repository.PollRepository
import kotlinx.coroutines.flow.*
import java.util.UUID

data class UiState(
    val polls: List<Poll> = emptyList(),
    val selectedCategory: PollCategory = PollCategory.ALL,
    val searchQuery: String = "",
    val snackbarMessage: String? = null,
    val username: String = "User",
    val votedPollIds: Set<String> = emptySet()
)

class PollViewModel : ViewModel() {

    private val repository = PollRepository()

    private val _selectedCategory = MutableStateFlow(PollCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _username = MutableStateFlow("Ranker")
    private val _votedPollIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<UiState> = combine(
        repository.polls,
        _selectedCategory,
        _searchQuery,
        _snackbarMessage,
        _username,
        _votedPollIds
    ) { args ->
        val polls = args[0] as List<Poll>
        val category = args[1] as PollCategory
        val query = args[2] as String
        val snackbar = args[3] as String?
        val username = args[4] as String
        val votedIds = args[5] as Set<String>

        val filteredAndSorted = polls
            .filter { category == PollCategory.ALL || it.category == category }
            .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
            .sortedWith(compareBy({ it.category.ordinal }, { -it.createdAt }))

        UiState(filteredAndSorted, category, query, snackbar, username, votedIds)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    // Exposed for individual consumption if preferred
    val polls = repository.polls
    val selectedCategory = _selectedCategory.asStateFlow()
    val searchQuery = _searchQuery.asStateFlow()
    val snackbarMessage = _snackbarMessage.asStateFlow()
    val username = _username.asStateFlow()

    fun setCategory(category: PollCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setUsername(newUsername: String) {
        _username.value = newUsername
    }

    fun vote(pollId: String, optionId: String, delta: Int) {
        repository.vote(pollId, optionId, delta)
        if (delta > 0) {
            _votedPollIds.value = _votedPollIds.value + pollId
        }
    }

    fun createPoll(
        title: String,
        category: PollCategory,
        optionNames: List<String>
    ): Boolean {
        if (title.isBlank()) {
            _snackbarMessage.value = "Poll title cannot be empty"
            return false
        }
        val uniqueOptions = optionNames.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (uniqueOptions.size < 2) {
            _snackbarMessage.value = "Add at least 2 unique options"
            return false
        }
        val poll = Poll(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            category = category,
            author = _username.value,
            emoji = category.emoji,
            options = uniqueOptions.mapIndexed { i, name ->
                PollOption(id = UUID.randomUUID().toString(), name = name, votes = 0)
            }
        )
        val success = repository.addPoll(poll)
        _snackbarMessage.value = if (success) "Poll created!" else "A poll with that title already exists"
        return success
    }

    fun addOptionToPoll(pollId: String, optionName: String): Boolean {
        if (optionName.isBlank()) {
            _snackbarMessage.value = "Option name cannot be empty"
            return false
        }
        val success = repository.addOptionToPoll(pollId, optionName)
        _snackbarMessage.value = if (success) "Option added!" else "That option already exists in this poll"
        return success
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun getFilteredPolls(polls: List<Poll>): List<Poll> {
        val category = _selectedCategory.value
        val query = _searchQuery.value
        return polls
            .filter { category == PollCategory.ALL || it.category == category }
            .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
            .sortedWith(compareBy({ it.category.ordinal }, { -it.createdAt }))
    }

    fun getUserCreatedPolls(): Flow<List<Poll>> {
        return repository.polls.map { polls ->
            polls.filter { it.author == _username.value }
        }
    }

    fun getUserVotedPolls(): Flow<List<Poll>> {
        return combine(repository.polls, _votedPollIds) { polls, votedIds ->
            polls.filter { it.id in votedIds }
        }
    }
}