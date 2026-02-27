package com.rankit.app.viewmodel

import androidx.lifecycle.ViewModel
import com.rankit.app.data.model.Poll
import com.rankit.app.data.model.PollCategory
import com.rankit.app.data.model.PollOption
import com.rankit.app.data.repository.PollRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.util.UUID

data class UiState(
    val polls: List<Poll> = emptyList(),
    val selectedCategory: PollCategory = PollCategory.ALL,
    val searchQuery: String = "",
    val snackbarMessage: String? = null
)

class PollViewModel : ViewModel() {

    private val repository = PollRepository()

    private val _selectedCategory = MutableStateFlow(PollCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState> = combine(
        repository.polls,
        _selectedCategory,
        _searchQuery,
        _snackbarMessage
    ) { polls, category, query, snackbar ->
        val filtered = polls
            .filter { category == PollCategory.ALL || it.category == category }
            .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
        UiState(filtered, category, query, snackbar)
    }.let { flow ->
        val stateFlow = MutableStateFlow(UiState())
        // Manually collect using init
        stateFlow
    }

    // Simpler approach: individual state flows
    val polls = repository.polls
    val selectedCategory = _selectedCategory.asStateFlow()
    val searchQuery = _searchQuery.asStateFlow()
    val snackbarMessage = _snackbarMessage.asStateFlow()

    fun setCategory(category: PollCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun vote(pollId: String, optionId: String, delta: Int) {
        repository.vote(pollId, optionId, delta)
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
            author = "You",
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
        return polls
            .filter { _selectedCategory.value == PollCategory.ALL || it.category == _selectedCategory.value }
            .filter { _searchQuery.value.isBlank() || it.title.contains(_searchQuery.value, ignoreCase = true) }
    }
}
