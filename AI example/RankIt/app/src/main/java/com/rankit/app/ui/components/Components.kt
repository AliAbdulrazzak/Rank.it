package com.rankit.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rankit.app.data.model.Poll
import com.rankit.app.data.model.PollOption
import com.rankit.app.ui.theme.*

@Composable
fun PollCard(
    poll: Poll,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedOptions = remember(poll.options) {
        poll.options.sortedByDescending { it.votes }
    }
    val topVotes = sortedOptions.firstOrNull()?.votes ?: 1
    val totalVotes = poll.options.sumOf { it.votes }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentPurple.copy(alpha = 0.15f))
                        .border(1.dp, AccentPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(poll.emoji, fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        poll.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        poll.category.displayName.uppercase(),
                        color = AccentPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AccentPurple.copy(alpha = 0.15f)
                ) {
                    Text(
                        "$totalVotes votes",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = AccentPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Top 3 options preview
            sortedOptions.take(3).forEachIndexed { index, option ->
                val pct = if (topVotes > 0) option.votes.toFloat() / topVotes else 0f
                val animPct by animateFloatAsState(
                    targetValue = pct,
                    animationSpec = tween(600),
                    label = "bar_${option.id}"
                )
                val medals = listOf("🥇", "🥈", "🥉")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .height(32.dp)
                ) {
                    // Background bar
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animPct)
                            .background(AccentPurple.copy(alpha = if (index == 0) 0.2f else 0.08f))
                    )
                    // Content
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(medals[index], fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            option.name,
                            modifier = Modifier.weight(1f),
                            color = if (index == 0) TextPrimary else TextSecondary,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${option.votes}",
                            color = AccentPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                if (index < 2) Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun VoteOptionRow(
    option: PollOption,
    rank: Int,
    maxVotes: Int,
    isTop: Boolean,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pct = if (maxVotes > 0) option.votes.toFloat() / maxVotes else 0f
    val animPct by animateFloatAsState(
        targetValue = pct,
        animationSpec = tween(500),
        label = "vote_bar_${option.id}"
    )
    val medals = listOf("🥇", "🥈", "🥉")
    val rankLabel = if (rank < 3) medals[rank] else "${rank + 1}"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isTop) AccentPurple.copy(0.4f) else CardBorder,
                RoundedCornerShape(14.dp)
            )
            .background(SurfaceDark)
    ) {
        // Vote bar background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animPct)
                .background(
                    if (isTop) AccentPurple.copy(0.18f) else AccentPurple.copy(0.07f)
                )
        )
        // Row content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(rankLabel, fontSize = 16.sp, modifier = Modifier.width(28.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    option.name,
                    color = if (isTop) TextPrimary else Color(0xFFCCCCCC),
                    fontWeight = if (isTop) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    "${(pct * 100).toInt()}% relative",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                "${option.votes}",
                color = AccentPurple,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(end = 10.dp)
            )
            // Vote buttons
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onUpvote,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(UpvoteGreen.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Upvote",
                        tint = UpvoteGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDownvote,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DownvoteRed.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Downvote",
                        tint = DownvoteRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) {
        Brush.linearGradient(listOf(AccentPurple, AccentBlue))
    } else {
        Brush.linearGradient(listOf(SurfaceVariantDark, SurfaceVariantDark))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, fontSize = 12.sp)
            Text(
                label,
                color = if (selected) TextPrimary else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}
