package com.kalindu.pocketfit.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.FitnessCenter
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.kalindu.pocketfit.ui.screens.HistoryScreen
import com.kalindu.pocketfit.ui.screens.ExerciseDetailScreen
import com.kalindu.pocketfit.ui.screens.ExerciseScreen
import com.kalindu.pocketfit.ui.screens.HomeScreen
import com.kalindu.pocketfit.ui.screens.LoginScreen
import com.kalindu.pocketfit.ui.screens.ProfileScreen
import com.kalindu.pocketfit.ui.screens.RegisterScreen
import com.kalindu.pocketfit.ui.screens.SessionDetailScreen
import com.kalindu.pocketfit.ui.screens.SessionScreen
import android.content.res.Configuration
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalindu.pocketfit.ui.viewmodel.AuthViewModel
import com.kalindu.pocketfit.ui.viewmodel.ExerciseViewModel
import com.kalindu.pocketfit.ui.viewmodel.HomeViewModel
import com.kalindu.pocketfit.ui.viewmodel.ProfileViewModel
import com.kalindu.pocketfit.ui.viewmodel.SessionViewModel

// Sealed class for navigation routes
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Sessions : Screen("sessions", "Sessions", Icons.Default.Timer)
    object Exercises : Screen("exercises", "Exercises", Icons.Default.FitnessCenter)
    object History : Screen("history", "History", Icons.Default.History)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object SessionDetail : Screen("session_detail/{sessionId}", "Session Details") {
        fun createRoute(sessionId: Int) = "session_detail/$sessionId"
    }
    object ExerciseDetail : Screen("exercise_detail/{exerciseId}", "Exercise Details") {
        fun createRoute(exerciseId: Int) = "exercise_detail/$exerciseId"
    }
}

// List of bottom navigation items
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Sessions,
    Screen.Exercises,
    Screen.Profile,
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val sessionViewModel: SessionViewModel = viewModel()
    val exerciseViewModel: ExerciseViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine start destination based on authentication status
    val startDestination = if (authViewModel.isUserLoggedIn()) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

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

    // Show the shared top bar for all screens except login
    val showTopBar = (currentRoute != null
            && currentRoute != Screen.Login.route)

    // Resolve the title for the top bar based on current route
    val topBarTitle = when (currentRoute) {
        Screen.Home.route -> "PocketFit"
        Screen.Sessions.route -> Screen.Sessions.title
        Screen.Exercises.route -> Screen.Exercises.title
        Screen.History.route -> Screen.History.title
        Screen.Profile.route -> Screen.Profile.title
        Screen.SessionDetail.route -> Screen.SessionDetail.title
        Screen.ExerciseDetail.route -> Screen.ExerciseDetail.title
        Screen.Register.route -> "Create Account"
        else -> ""
    }

    // Show a back arrow on detail and register screens
    val showBackArrow = currentRoute in listOf(
        Screen.SessionDetail.route,
        Screen.ExerciseDetail.route,
        Screen.History.route,
        Screen.Register.route
    )

    SessionTrackingPermissionEffect(sessionViewModel)

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
                    actions = {
                        if (currentRoute == Screen.Sessions.route) {
                            IconButton(
                                onClick = {
                                    navController.navigate(Screen.History.route)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Session History"
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
            startDestination = startDestination,
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
                    authViewModel = authViewModel,
                    onLoginSuccess = {
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
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Home route
            composable(Screen.Home.route) {
                HomeScreen(
                    authViewModel = authViewModel,
                    homeViewModel = homeViewModel,
                    sessionViewModel = sessionViewModel
                )
            }

            // Sessions route
            composable(Screen.Sessions.route) {
                SessionScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                    },
                    viewModel = sessionViewModel
                )
            }

            // History route
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = sessionViewModel,
                    onSessionClick = { sessionId ->
                        navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                    }
                )
            }

            // Exercises route
            composable(Screen.Exercises.route) {
                ExerciseScreen(
                    viewModel = exerciseViewModel,
                    onExerciseClick = { exerciseId ->
                        navController.navigate(
                            Screen.ExerciseDetail.createRoute(exerciseId)
                        )
                    }
                )
            }

            // Profile route
            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel()
                ProfileScreen(
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel,
                    sessionViewModel = sessionViewModel,
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Session detail route
            composable(
                route = Screen.SessionDetail.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: 0
                SessionDetailScreen(
                    sessionId = sessionId,
                    viewModel = sessionViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Exercise detail route
            composable(
                route = Screen.ExerciseDetail.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.IntType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getInt("exerciseId") ?: 0
                ExerciseDetailScreen(
                    exerciseId = exerciseId,
                    viewModel = exerciseViewModel
                )
            }
        }
    }
}

@Composable
private fun SessionTrackingPermissionEffect(viewModel: SessionViewModel) {
    val context = LocalContext.current
    val activeSession by viewModel.activeSession.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startStepTracking()
        } else {
            viewModel.reportStepPermissionDenied()
        }
    }

    LaunchedEffect(activeSession?.id) {
        if (activeSession == null) {
            viewModel.stopStepTracking()
            return@LaunchedEffect
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.startStepTracking()
        } else {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
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
