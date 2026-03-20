package com.rankit.app.data.model

data class PollOption(
    val id: String,
    val name: String,
    val votes: Int = 0,
    val imageUrl: String? = null,
    val emoji: String = "🔵"
)

data class Poll(
    val id: String,
    val title: String,
    val category: PollCategory,
    val author: String,
    val emoji: String,
    val options: List<PollOption>,
    val createdAt: Long = System.currentTimeMillis()
)

enum class PollCategory(val displayName: String, val emoji: String) {
    ALL("All", "🌐"),
    SPORTS("Sports", "🏆"),
    MUSIC("Music", "🎵"),
    MOVIES("Movies", "🎬"),
    TV_SHOWS("TV Shows", "📺"),
    FOOD("Food", "🍕"),
    GAMING("Gaming", "🎮"),
    HISTORY("History", "📜"),
    OTHER("Other", "✨")
}
