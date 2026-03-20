package com.rankit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rankit.app.data.model.Poll
import com.rankit.app.ui.theme.*
import com.rankit.app.viewmodel.PollViewModel

@Composable
fun TrendingScreen(
    viewModel: PollViewModel,
    onPollClick: (Poll) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val allPolls = uiState.polls

    val topVotedPolls = remember(allPolls) {
        allPolls.sortedByDescending { it.options.sumOf { option -> option.votes } }.take(3)
    }

    val newestPolls = remember(allPolls) {
        allPolls.sortedByDescending { it.createdAt }.take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Text(
            text = "Trending",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                TrendingSection(
                    title = "Most Popular",
                    icon = Icons.Default.TrendingUp,
                    polls = topVotedPolls,
                    onPollClick = onPollClick,
                    accentColor = AccentPurple
                )
            }

            item {
                TrendingSection(
                    title = "Recently Added",
                    icon = Icons.Default.NewReleases,
                    polls = newestPolls,
                    onPollClick = onPollClick,
                    accentColor = AccentBlue
                )
            }
        }
    }
}

@Composable
fun TrendingSection(
    title: String,
    icon: ImageVector,
    polls: List<Poll>,
    onPollClick: (Poll) -> Unit,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (polls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No polls found",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            polls.forEachIndexed { index, poll ->
                TrendingPollItem(
                    poll = poll,
                    rank = index + 1,
                    onPollClick = { onPollClick(poll) },
                    accentColor = accentColor
                )
                if (index < polls.size - 1) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun TrendingPollItem(
    poll: Poll,
    rank: Int,
    onPollClick: () -> Unit,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPollClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = poll.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = "${poll.options.sumOf { it.votes }} votes • ${poll.category.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Text(
                text = poll.emoji,
                fontSize = 20.sp
            )
        }
    }
}
