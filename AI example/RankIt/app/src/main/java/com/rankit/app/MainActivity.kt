package com.rankit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rankit.app.data.model.Poll
import com.rankit.app.ui.screens.*
import com.rankit.app.ui.theme.BackgroundDark
import com.rankit.app.ui.theme.RankItTheme
import com.rankit.app.ui.theme.TextMuted
import com.rankit.app.ui.theme.AccentPurple
import com.rankit.app.viewmodel.PollViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Trending : Screen("trending")
    object PollDetail : Screen("poll_detail")
    object CreatePoll : Screen("create_poll")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
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

    val tabs = listOf(
        Triple("Home", Icons.Default.Home, Screen.Home.route),
        Triple("Trending", Icons.Default.Star, Screen.Trending.route),
        Triple("Profile", Icons.Default.Person, Screen.Profile.route),
        Triple("Settings", Icons.Default.Settings, Screen.Settings.route)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val routeOrder = listOf(
        Screen.Home.route,
        Screen.Trending.route,
        Screen.Profile.route,
        Screen.Settings.route
    )

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = BackgroundDark.copy(alpha = 0.97f),
                tonalElevation = 0.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                tabs.forEach { (label, icon, route) ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
                val initialStateOrder = routeOrder.indexOf(initialState.destination.route)
                val targetStateOrder = routeOrder.indexOf(targetState.destination.route)

                if (initialStateOrder != -1 && targetStateOrder != -1) {
                    if (targetStateOrder > initialStateOrder) {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                                fadeIn(animationSpec = tween(300))
                    } else {
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) +
                                fadeIn(animationSpec = tween(300))
                    }
                } else {
                    fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                val initialStateOrder = routeOrder.indexOf(initialState.destination.route)
                val targetStateOrder = routeOrder.indexOf(targetState.destination.route)

                if (initialStateOrder != -1 && targetStateOrder != -1) {
                    if (targetStateOrder > initialStateOrder) {
                        slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) +
                                fadeOut(animationSpec = tween(300))
                    } else {
                        slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = tween(300)) +
                                fadeOut(animationSpec = tween(300))
                    }
                } else {
                    fadeOut(animationSpec = tween(300))
                }
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
            composable(Screen.Trending.route) {
                TrendingScreen(
                    viewModel = viewModel,
                    onPollClick = { poll ->
                        selectedPoll = poll
                        navController.navigate(Screen.PollDetail.route)
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
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onPollClick = { poll ->
                        selectedPoll = poll
                        navController.navigate(Screen.PollDetail.route)
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}