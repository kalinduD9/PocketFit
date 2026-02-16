package com.kalindu.pocketfit.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kalindu.pocketfit.ui.screens.ActivityDetailScreen
import com.kalindu.pocketfit.ui.screens.ActivityScreen
import com.kalindu.pocketfit.ui.screens.HistoryScreen
import com.kalindu.pocketfit.ui.screens.HomeScreen
import com.kalindu.pocketfit.ui.screens.LoginScreen
import com.kalindu.pocketfit.ui.screens.ProfileScreen
import com.kalindu.pocketfit.ui.screens.RegisterScreen
import android.content.res.Configuration
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration

// Sealed class for navigation routes
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Activity : Screen("activity", "Activity", Icons.AutoMirrored.Filled.DirectionsRun)
    object History : Screen("history", "History", Icons.Default.History)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object ActivityDetail : Screen("activity_detail/{activityId}", "Activity Details") {
        fun createRoute(activityId: Int) = "activity_detail/$activityId"
    }
}

// List of bottom navigation items
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Activity,
    Screen.History,
    Screen.Profile
)


// ... (existing code)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine orientation
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Only scroll in landscape
    val scrollBehavior = if (isLandscape) {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    } else {
        TopAppBarDefaults.pinnedScrollBehavior()
    }

    // Determine which screens show the bottom bar
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    // Show the shared top bar for all screens except login and register
    val showTopBar = currentRoute != null
            && currentRoute != Screen.Login.route
            && currentRoute != Screen.Register.route

    // Resolve the title for the top bar based on current route
    val topBarTitle = when (currentRoute) {
        Screen.Home.route -> "PocketFit"
        Screen.Activity.route -> Screen.Activity.title
        Screen.History.route -> Screen.History.title
        Screen.Profile.route -> Screen.Profile.title
        Screen.ActivityDetail.route -> Screen.ActivityDetail.title
        else -> ""
    }

    // Show a back arrow on detail screens
    val showBackArrow = currentRoute == Screen.ActivityDetail.route

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        if (showBackArrow) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        scrolledContainerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        val animDuration = 300

        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(paddingValues),
            // Animated transitions for navigation
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(animDuration)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(animDuration)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(animDuration)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(animDuration)
                )
            }
        ) {
            // Navigation routes
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            // Register route
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Home route
            composable(Screen.Home.route) {
                HomeScreen()
            }

            // Activity route
            composable(Screen.Activity.route) {
                ActivityScreen(
                    onActivityClick = { activityId ->
                        navController.navigate(Screen.ActivityDetail.createRoute(activityId))
                    }
                )
            }

            // History route
            composable(Screen.History.route) {
                HistoryScreen()
            }

            // Profile route
            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            // Activity detail route
            composable(
                route = Screen.ActivityDetail.route,
                arguments = listOf(navArgument("activityId") { type = NavType.IntType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getInt("activityId") ?: 0
                ActivityDetailScreen(
                    activityId = activityId
                )
            }
        }
    }
}

// Bottom navigation bar
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = {
                    screen.icon?.let { Icon(it, contentDescription = screen.title) }
                },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}