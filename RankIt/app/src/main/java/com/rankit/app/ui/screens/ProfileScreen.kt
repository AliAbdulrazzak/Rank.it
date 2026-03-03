package com.rankit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rankit.app.data.model.Poll
import com.rankit.app.ui.components.PollCard
import com.rankit.app.ui.theme.*
import com.rankit.app.viewmodel.PollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: PollViewModel,
    onPollClick: (Poll) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val createdPolls by viewModel.getUserCreatedPolls().collectAsStateWithLifecycle(initialValue = emptyList())
    val votedPolls by viewModel.getUserVotedPolls().collectAsStateWithLifecycle(initialValue = emptyList())
    
    var showEditUsername by remember { mutableStateOf(false) }
    var tempUsername by remember { mutableStateOf(uiState.username) }

    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header with User Profile
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(SurfaceDark, BackgroundDark)
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(AccentPurple.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                uiState.username.take(1).uppercase(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurple
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                uiState.username,
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { 
                                tempUsername = uiState.username
                                showEditUsername = true 
                            }) {
                                Icon(Icons.Default.Edit, "Edit username", tint = TextMuted, modifier = Modifier.size(20.dp))
                            }
                        }
                        
                        Text(
                            "Member since 2024",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Tabs Created/Voted
            item {
                Text(
                    "Polls Created (${createdPolls.size})",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (createdPolls.isEmpty()) {
                item {
                    Text(
                        "You haven't created any polls yet.",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                items(createdPolls) { poll ->
                    PollCard(
                        poll = poll,
                        onClick = { onPollClick(poll) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Polls Voted On (${votedPolls.size})",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (votedPolls.isEmpty()) {
                item {
                    Text(
                        "You haven't voted on any polls yet.",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                items(votedPolls) { poll ->
                    PollCard(
                        poll = poll,
                        onClick = { onPollClick(poll) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showEditUsername) {
        AlertDialog(
            onDismissRequest = { showEditUsername = false },
            containerColor = SurfaceDark,
            title = { Text("Edit Username", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = tempUsername,
                    onValueChange = { tempUsername = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = CardBorder
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempUsername.isNotBlank()) {
                        viewModel.setUsername(tempUsername)
                        showEditUsername = false
                    }
                }) {
                    Text("Save", color = AccentPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUsername = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
