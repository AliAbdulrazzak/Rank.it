package com.rankit.app.data.repository

import com.rankit.app.data.model.Poll
import com.rankit.app.data.model.PollCategory
import com.rankit.app.data.model.PollOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class PollRepository {

    private val _polls = MutableStateFlow(getSamplePolls())
    val polls: StateFlow<List<Poll>> = _polls.asStateFlow()

    fun vote(pollId: String, optionId: String, delta: Int) {
        _polls.value = _polls.value.map { poll ->
            if (poll.id != pollId) poll
            else poll.copy(
                options = poll.options.map { option ->
                    if (option.id != optionId) option
                    else option.copy(votes = maxOf(0, option.votes + delta))
                }
            )
        }
    }

    fun addPoll(poll: Poll): Boolean {
        // Check for duplicate title (case-insensitive)
        val exists = _polls.value.any {
            it.title.trim().equals(poll.title.trim(), ignoreCase = true)
        }
        if (exists) return false
        _polls.value = listOf(poll) + _polls.value
        return true
    }

    fun addOptionToPoll(pollId: String, optionName: String): Boolean {
        val poll = _polls.value.find { it.id == pollId } ?: return false
        // Check for duplicate option within this poll
        val exists = poll.options.any {
            it.name.trim().equals(optionName.trim(), ignoreCase = true)
        }
        if (exists) return false
        val newOption = PollOption(
            id = UUID.randomUUID().toString(),
            name = optionName.trim(),
            votes = 0
        )
        _polls.value = _polls.value.map { p ->
            if (p.id != pollId) p
            else p.copy(options = p.options + newOption)
        }
        return true
    }

    private fun getSamplePolls(): List<Poll> = listOf(
        Poll(
            id = "1",
            title = "The 12 Greek Olympians",
            category = PollCategory.HISTORY,
            author = "Ali A.",
            emoji = "⚡",
            options = listOf(
                PollOption("1a", "Zeus", 142, emoji = "👑"),
                PollOption("1b", "Athena", 138, emoji = "🦉"),
                PollOption("1c", "Apollo", 121, emoji = "☀️"),
                PollOption("1d", "Poseidon", 98, emoji = "🔱"),
                PollOption("1e", "Artemis", 87, emoji = "🌙"),
                PollOption("1f", "Hephaestus", 54, emoji = "🔥"),
                PollOption("1g", "Ares", 49, emoji = "⚔️"),
                PollOption("1h", "Aphrodite", 76, emoji = "💘"),
                PollOption("1i", "Hermes", 65, emoji = "🪶"),
                PollOption("1j", "Demeter", 42, emoji = "🌾"),
                PollOption("1k", "Dionysus", 38, emoji = "🍇"),
                PollOption("1l", "Hera", 55, emoji = "👸")
            )
        ),
        Poll(
            id = "2",
            title = "Greatest NBA Players of All Time",
            category = PollCategory.SPORTS,
            author = "Jordan B.",
            emoji = "🏀",
            options = listOf(
                PollOption("2a", "Michael Jordan", 334, emoji = "🐐"),
                PollOption("2b", "LeBron James", 298, emoji = "👑"),
                PollOption("2c", "Kobe Bryant", 261, emoji = "🐍"),
                PollOption("2d", "Magic Johnson", 178, emoji = "✨"),
                PollOption("2e", "Kareem Abdul-Jabbar", 145, emoji = "🏆")
            )
        ),
        Poll(
            id = "3",
            title = "Best Albums of the 2010s",
            category = PollCategory.MUSIC,
            author = "Heather C.",
            emoji = "🎵",
            options = listOf(
                PollOption("3a", "Kendrick Lamar – TPAB", 201, emoji = "🎤"),
                PollOption("3b", "Beyoncé – Lemonade", 188, emoji = "🍋"),
                PollOption("3c", "Frank Ocean – Blonde", 176, emoji = "🌊"),
                PollOption("3d", "Kanye West – MBDTF", 154, emoji = "🎨"),
                PollOption("3e", "Taylor Swift – 1989", 133, emoji = "🎸")
            )
        ),
        Poll(
            id = "4",
            title = "Best Sci-Fi Movies Ever",
            category = PollCategory.MOVIES,
            author = "Eddy U.",
            emoji = "🚀",
            options = listOf(
                PollOption("4a", "Interstellar", 289, emoji = "🌌"),
                PollOption("4b", "2001: A Space Odyssey", 244, emoji = "🛸"),
                PollOption("4c", "Blade Runner 2049", 198, emoji = "🤖"),
                PollOption("4d", "The Matrix", 187, emoji = "💊"),
                PollOption("4e", "Arrival", 156, emoji = "🛰️")
            )
        )
    )
}
