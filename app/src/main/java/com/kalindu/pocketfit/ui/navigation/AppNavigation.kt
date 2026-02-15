package com.kalindu.pocketfit.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.kalindu.pocketfit.ui.screens.LoginScreen
import com.kalindu.pocketfit.ui.screens.RegisterScreen
import com.kalindu.pocketfit.ui.screens.HomeScreen
import com.kalindu.pocketfit.ui.screens.ProfileScreen

// Sealed class for navigation routes
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Activity : Screen("activity", "Activity", Icons.Default.DirectionsRun)
    object History : Screen("history", "History", Icons.Default.History)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object ActivityDetail : Screen("activity_detail/{activityId}", "Activity Detail") {
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginClick = {
                        // Navigate to Home screen
                        navController.navigate(Screen.Home.route) {
                            // Clear the back stack so user can't go back to login
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        // Navigate to Register screen
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterClick = {
                        // Navigate to Home screen
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        // Navigate back to Login
                        navController.popBackStack()
                    },
                    onBackClick = {
                        // Navigate back to Login
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen()
            }

            composable(Screen.Activity.route) {
                ActivityScreen(
                    onActivityClick = { activityId ->
                        // Navigate to Activity Detail
                        navController.navigate(Screen.ActivityDetail.createRoute(activityId))
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            composable(
                route = Screen.ActivityDetail.route,
                arguments = listOf(navArgument("activityId") { type = NavType.IntType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getInt("activityId") ?: 0
                ActivityDetailScreen(
                    activityId = activityId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom nav on main screens
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    if (showBottomBar) {
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
}