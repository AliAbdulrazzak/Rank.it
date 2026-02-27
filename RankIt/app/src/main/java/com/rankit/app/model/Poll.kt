package com.rankit.app.model

data class PollOption(
    val id: Int,
    val name: String,
    var votes: Int = 0,
    val imageUrl: String? = null
)

data class Poll(
    val id: Int,
    val question: String,
    val category: Category,
    val options: MutableList<PollOption>,
    val createdBy: String = "anonymous"
)

enum class Category(val displayName: String, val emoji: String) {
    ALL("All", "⚡"),
    SPORTS("Sports", "🏈"),
    MUSIC("Music", "🎵"),
    MOVIES("Movies", "🎬"),
    TECH("Tech", "💻"),
    FOOD("Food", "🍕")
}

// Seed data
fun getSeedPolls(): List<Poll> = listOf(
    Poll(
        id = 1,
        question = "Who is the greatest QB of all time?",
        category = Category.SPORTS,
        options = mutableListOf(
            PollOption(1, "Tom Brady", 2847),
            PollOption(2, "Joe Montana", 1923),
            PollOption(3, "Patrick Mahomes", 1654),
            PollOption(4, "Peyton Manning", 1201),
            PollOption(5, "Aaron Rodgers", 987)
        )
    ),
    Poll(
        id = 2,
        question = "Greatest rapper of all time?",
        category = Category.MUSIC,
        options = mutableListOf(
            PollOption(1, "Kendrick Lamar", 3102),
            PollOption(2, "Jay-Z", 2541),
            PollOption(3, "Eminem", 2388),
            PollOption(4, "Nas", 1876),
            PollOption(5, "Lil Wayne", 1543)
        )
    ),
    Poll(
        id = 3,
        question = "Best sci-fi film ever made?",
        category = Category.MOVIES,
        options = mutableListOf(
            PollOption(1, "Interstellar", 4201),
            PollOption(2, "2001: A Space Odyssey", 3876),
            PollOption(3, "Blade Runner 2049", 2943),
            PollOption(4, "The Matrix", 2711),
            PollOption(5, "Dune", 1998)
        )
    ),
    Poll(
        id = 4,
        question = "Best NBA player of all time?",
        category = Category.SPORTS,
        options = mutableListOf(
            PollOption(1, "Michael Jordan", 5120),
            PollOption(2, "LeBron James", 4890),
            PollOption(3, "Kareem Abdul-Jabbar", 2100),
            PollOption(4, "Magic Johnson", 1800),
            PollOption(5, "Kobe Bryant", 3400)
        )
    ),
    Poll(
        id = 5,
        question = "Best programming language?",
        category = Category.TECH,
        options = mutableListOf(
            PollOption(1, "Python", 3800),
            PollOption(2, "JavaScript", 3200),
            PollOption(3, "Kotlin", 1500),
            PollOption(4, "Rust", 1200),
            PollOption(5, "Go", 900)
        )
    )
)
