package com.rankit.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rankit.app.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rankit.app.data.model.Poll
import com.rankit.app.data.model.PollCategory
import com.rankit.app.ui.components.CategoryChip
import com.rankit.app.ui.components.PollCard
import com.rankit.app.ui.theme.*
import com.rankit.app.viewmodel.PollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PollViewModel,
    onPollClick: (Poll) -> Unit,
    onCreateClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = AccentPurple,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, "Create Poll", tint = TextPrimary)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // App Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(SurfaceDark, BackgroundDark)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ── Logo + App Name ──────────────────────────
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.rankit_logo_2),
                                    contentDescription = "RankIt Logo",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .padding(end = 10.dp)
                                )
                                Column {
                                    Text(
                                        buildString {
                                            append("Rank")
                                            append("It")
                                        },
                                        color = TextPrimary,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        "Community Voting",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SurfaceVariantDark, RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Default.Notifications, "Notifications", tint = TextSecondary)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Search bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search polls...", color = TextMuted, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = SurfaceVariantDark,
                                unfocusedContainerColor = SurfaceVariantDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Category filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(PollCategory.values()) { category ->
                        CategoryChip(
                            label = category.displayName,
                            emoji = category.emoji,
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.setCategory(category) }
                        )
                    }
                }
            }

            // Section label
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (uiState.selectedCategory == PollCategory.ALL) "All Polls" else "${uiState.selectedCategory.displayName} Polls",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${uiState.polls.size} polls",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
            }

            // Poll cards
            if (uiState.polls.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗳️", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("No polls here yet", color = TextSecondary, fontSize = 14.sp)
                            Text("Be the first to create one!", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(uiState.polls, key = { it.id }) { poll ->
                    PollCard(
                        poll = poll,
                        onClick = { onPollClick(poll) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}
