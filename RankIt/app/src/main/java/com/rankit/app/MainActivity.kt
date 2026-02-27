package com.rankit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rankit.app.data.model.Poll
import com.rankit.app.ui.screens.CreatePollScreen
import com.rankit.app.ui.screens.HomeScreen
import com.rankit.app.ui.screens.PollDetailScreen
import com.rankit.app.ui.theme.BackgroundDark
import com.rankit.app.ui.theme.CardBorder
import com.rankit.app.ui.theme.RankItTheme
import com.rankit.app.ui.theme.TextMuted
import com.rankit.app.ui.theme.TextPrimary
import com.rankit.app.ui.theme.TextSecondary
import com.rankit.app.ui.theme.AccentPurple
import com.rankit.app.viewmodel.PollViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PollDetail : Screen("poll_detail")
    object CreatePoll : Screen("create_poll")
    object Profile : Screen("profile")
}

class MainActivity : ComponentActivity() {

    private val viewModel: PollViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RankItTheme {
                RankItApp(viewModel)
            }
        }
    }
}

@Composable
fun RankItApp(viewModel: PollViewModel) {
    val navController = rememberNavController()
    var selectedPoll by remember { mutableStateOf<Poll?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        Pair("Home", Icons.Default.Home),
        Pair("Trending", Icons.Default.Star),
        Pair("Profile", Icons.Default.Person),
        Pair("Settings", Icons.Default.Settings)
    )

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = BackgroundDark.copy(alpha = 0.97f),
                tonalElevation = 0.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                tabs.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            if (index == 0) navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentPurple,
                            selectedTextColor = AccentPurple,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = AccentPurple.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                        fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) +
                        fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) +
                        fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) +
                        fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(Screen.Home.route) {
                selectedTab = 0
                HomeScreen(
                    viewModel = viewModel,
                    onPollClick = { poll ->
                        selectedPoll = poll
                        navController.navigate(Screen.PollDetail.route)
                    },
                    onCreateClick = {
                        navController.navigate(Screen.CreatePoll.route)
                    }
                )
            }
            composable(Screen.PollDetail.route) {
                selectedPoll?.let { poll ->
                    PollDetailScreen(
                        poll = poll,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Screen.CreatePoll.route) {
                CreatePollScreen(
                    viewModel = viewModel,
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}
