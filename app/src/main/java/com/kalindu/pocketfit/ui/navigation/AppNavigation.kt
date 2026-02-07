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
                // LoginScreen will be created next
                Text("Login Screen - Coming Soon")
            }

            composable(Screen.Register.route) {
                Text("Register Screen - Coming Soon")
            }

            composable(Screen.Home.route) {
                Text("Home Screen - Coming Soon")
            }

            composable(Screen.Activity.route) {
                Text("Activity Screen - Coming Soon")
            }

            composable(Screen.History.route) {
                Text("History Screen - Coming Soon")
            }

            composable(Screen.Profile.route) {
                Text("Profile Screen - Coming Soon")
            }

            composable(
                route = Screen.ActivityDetail.route,
                arguments = listOf(navArgument("activityId") { type = NavType.IntType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getInt("activityId") ?: 0
                Text("Activity Detail Screen - Activity ID: $activityId")
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