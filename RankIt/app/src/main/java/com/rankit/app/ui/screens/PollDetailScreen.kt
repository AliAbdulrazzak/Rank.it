package com.rankit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rankit.app.data.model.Poll
import com.rankit.app.data.model.PollOption
import com.rankit.app.ui.components.VoteOptionRow
import com.rankit.app.ui.theme.*
import com.rankit.app.viewmodel.PollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollDetailScreen(
    poll: Poll,
    viewModel: PollViewModel,
    onBack: () -> Unit
) {
    val polls by viewModel.polls.collectAsState()
    val currentPoll = polls.find { it.id == poll.id } ?: poll
    val sortedOptions = remember(currentPoll.options) {
        currentPoll.options.sortedByDescending { it.votes }
    }
    val maxVotes = sortedOptions.firstOrNull()?.votes?.takeIf { it > 0 } ?: 1
    val totalVotes = currentPoll.options.sumOf { it.votes }

    var showAddOption by remember { mutableStateOf(false) }
    var newOptionText by remember { mutableStateOf("") }

    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(SurfaceDark, BackgroundDark))
                        )
                        .padding(bottom = 20.dp)
                ) {
                    Column {
                        // Back button
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Back", color = TextSecondary, fontSize = 13.sp)
                        }

                        // Poll info
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(currentPoll.emoji, fontSize = 40.sp, modifier = Modifier.padding(end = 16.dp))
                            Column {
                                Text(
                                    currentPoll.title,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 26.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AccentPurple.copy(0.15f)
                                    ) {
                                        Text(
                                            currentPoll.category.displayName,
                                            color = AccentPurple,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text("by ${currentPoll.author}", color = TextMuted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterVertically))
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "$totalVotes total votes • ${currentPoll.options.size} options",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section label
            item {
                Text(
                    "RANKINGS • TAP TO VOTE",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Vote options
            itemsIndexed(sortedOptions, key = { _, o -> o.id }) { index, option ->
                VoteOptionRow(
                    option = option,
                    rank = index,
                    maxVotes = maxVotes,
                    isTop = index == 0,
                    onUpvote = { viewModel.vote(currentPoll.id, option.id, 1) },
                    onDownvote = { viewModel.vote(currentPoll.id, option.id, -1) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Add option button
            item {
                Spacer(Modifier.height(8.dp))
                if (showAddOption) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = newOptionText,
                            onValueChange = { newOptionText = it },
                            placeholder = { Text("New option name...", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = SurfaceVariantDark,
                                unfocusedContainerColor = SurfaceVariantDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    showAddOption = false
                                    newOptionText = ""
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Text("Cancel", color = TextSecondary)
                            }
                            Button(
                                onClick = {
                                    if (viewModel.addOptionToPoll(currentPoll.id, newOptionText)) {
                                        newOptionText = ""
                                        showAddOption = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                            ) {
                                Text("Add Option")
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showAddOption = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            CardBorder
                        )
                    ) {
                        Text("+ Add an Option", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
