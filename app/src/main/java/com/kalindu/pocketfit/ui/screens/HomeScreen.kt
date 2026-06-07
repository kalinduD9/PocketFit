package com.kalindu.pocketfit.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kalindu.pocketfit.data.repository.WeatherSource
import com.kalindu.pocketfit.ui.viewmodel.AuthViewModel
import com.kalindu.pocketfit.ui.viewmodel.HomeViewModel
import com.kalindu.pocketfit.ui.viewmodel.SessionSensorState
import com.kalindu.pocketfit.ui.viewmodel.SessionViewModel
import com.kalindu.pocketfit.ui.viewmodel.WeatherUiState
import com.kalindu.pocketfit.utils.LocationHelper
import com.kalindu.pocketfit.utils.DailyGoalsValidation
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    sessionViewModel: SessionViewModel
) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val activeSession by sessionViewModel.activeSession.collectAsState()
    val todaySessions by sessionViewModel.todaySessions.collectAsState()
    val sensorState by sessionViewModel.sensorState.collectAsState()
    val dailyGoals by homeViewModel.dailyGoals.collectAsState()
    val goalsMessage by homeViewModel.goalsMessage.collectAsState()
    var showGoalsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(goalsMessage) {
        goalsMessage?.let {
            snackbarHostState.showSnackbar(it)
            homeViewModel.clearGoalsMessage()
        }
    }

    // Permission Launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            homeViewModel.setWeatherLoading()
            locationHelper.getCurrentLocation { location ->
                if (location != null) {
                    homeViewModel.getWeatherForLocation(location.latitude, location.longitude)
                } else {
                    homeViewModel.loadOfflineWeather()
                }
            }
        } else {
            homeViewModel.loadOfflineWeather()
        }
    }

    // Initialize weather fetching.
    LaunchedEffect(Unit) {
        val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                          ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasLocation) {
            locationHelper.getCurrentLocation { location ->
                if (location != null) {
                    homeViewModel.getWeatherForLocation(location.latitude, location.longitude)
                } else {
                    homeViewModel.loadOfflineWeather()
                }
            }
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    val scrollState = rememberScrollState()
    val weatherState by homeViewModel.weatherState
    val totalSteps = todaySessions.sumOf { it.steps }
    val totalCalories = todaySessions.sumOf { it.calories }
    val stepsProgress = DailyGoalsValidation.progress(totalSteps, dailyGoals.stepGoal)
    val caloriesProgress =
        DailyGoalsValidation.progress(totalCalories, dailyGoals.calorieGoal)
    val firstName = authViewModel.currentUserName
        .trim()
        .substringBefore(' ')
        .ifBlank { "User" }

    if (showGoalsDialog) {
        EditDailyGoalsDialog(
            initialStepGoal = dailyGoals.stepGoal,
            initialCalorieGoal = dailyGoals.calorieGoal,
            onDismiss = { showGoalsDialog = false },
            onSave = { stepGoal, calorieGoal ->
                if (homeViewModel.saveDailyGoals(stepGoal, calorieGoal)) {
                    showGoalsDialog = false
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(scaffoldPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = "Welcome Back, $firstName!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center,
            )
        }

        // Weather Status Card
        WeatherStatusCard(weatherState, onRetry = {
            homeViewModel.setWeatherLoading()
            locationHelper.getCurrentLocation { loc ->
                if (loc != null) {
                    homeViewModel.getWeatherForLocation(loc.latitude, loc.longitude)
                } else {
                    homeViewModel.loadOfflineWeather()
                }
            }
        })

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Daily Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${dailyGoals.stepGoal} steps  •  ${dailyGoals.calorieGoal} kcal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = { showGoalsDialog = true }) {
                    Text("Edit")
                }
            }
        }

        // Steps tracked by today's sessions.
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Steps Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                    CircularProgressIndicator(
                        progress = { stepsProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 12.dp,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalSteps.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "steps",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (activeSession != null &&
                    sensorState in listOf(
                        SessionSensorState.UNAVAILABLE,
                        SessionSensorState.PERMISSION_DENIED
                    )
                ) {
                    Text(
                        text = if (sensorState == SessionSensorState.PERMISSION_DENIED) {
                            "Activity recognition permission is denied. Time will still be tracked."
                        } else {
                            "Step sensor is unavailable. Time will still be tracked."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Text(
                    text = "Daily goal: ${dailyGoals.stepGoal} steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        // Estimated calories burned by daily activities
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Calories Burned", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "🔥", fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = totalCalories.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { caloriesProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "$totalCalories / ${dailyGoals.calorieGoal} kcal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Estimated from session steps and saved profile weight.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

    }
    }
}

@Composable
private fun EditDailyGoalsDialog(
    initialStepGoal: Int,
    initialCalorieGoal: Int,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var stepGoal by remember(initialStepGoal) {
        mutableStateOf(initialStepGoal.toString())
    }
    var calorieGoal by remember(initialCalorieGoal) {
        mutableStateOf(initialCalorieGoal.toString())
    }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Daily Goals", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = stepGoal,
                    onValueChange = {
                        stepGoal = it
                        validationError = null
                    },
                    label = { Text("Daily step goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = calorieGoal,
                    onValueChange = {
                        calorieGoal = it
                        validationError = null
                    },
                    label = { Text("Daily calorie goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                validationError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validation = DailyGoalsValidation.validate(stepGoal, calorieGoal)
                    if (validation.isValid) {
                        onSave(stepGoal, calorieGoal)
                    } else {
                        validationError = validation.message
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WeatherStatusCard(state: WeatherUiState, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().then(if (state is WeatherUiState.Error) Modifier.clickable { onRetry() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = if (state is WeatherUiState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (state) {
                is WeatherUiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                is WeatherUiState.Success -> {
                    val weatherData = state.data
                    val weather = weatherData.weather.weather.firstOrNull()
                    val temp = weatherData.weather.main.temp.toInt()
                    val city = weatherData.weather.cityName
                    val (status, tip, icon) = when {
                        weatherData.source == WeatherSource.BUNDLED ->
                            Triple(
                                "Offline guidance",
                                "Live weather is unavailable. An indoor workout is a reliable choice.",
                                "🏠"
                            )
                        weather?.main == "Rain" || weather?.main == "Thunderstorm" || weather?.main == "Drizzle" -> Triple("Rainy", "Likely to rain. Indoor workout recommended! 🏠", "🌧️")
                        weather?.main == "Clouds" -> {
                            val description = weather.description.lowercase()
                            if (description.contains("overcast") || description.contains("broken")) Triple("Overcast", "Heavy clouds, might rain soon. Stay close to home! ☁️", "☁️")
                            else Triple("Partly Cloudy", "Good weather for a run before it gets cloudy! 🌤️", "⛅")
                        }
                        weather?.main == "Clear" -> Triple("Sunny", "Clear skies! Perfect for an outdoor run! ☀️", "☀️")
                        else -> Triple(weather?.main ?: "Clear", "Looking good for some activity! 💪", "✨")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(text = icon, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                            Column {
                                Text(
                                    text = if (weatherData.source == WeatherSource.BUNDLED) {
                                        status
                                    } else {
                                        "$status in $city"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                if (weatherData.source != WeatherSource.BUNDLED) {
                                    Text(
                                        text = "$temp°C",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f), shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                            Text(text = tip, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onTertiaryContainer, textAlign = TextAlign.Center, modifier = Modifier.padding(8.dp))
                        }
                        if (weatherData.source != WeatherSource.LIVE) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val offlineLabel = when (weatherData.source) {
                                WeatherSource.CACHED -> {
                                    val savedAt = weatherData.savedAtEpochMillis?.let {
                                        DateFormat.getDateTimeInstance(
                                            DateFormat.MEDIUM,
                                            DateFormat.SHORT
                                        ).format(Date(it))
                                    }
                                    if (savedAt == null) "Offline: saved weather" else "Offline: saved $savedAt"
                                }
                                WeatherSource.BUNDLED -> "Offline: general fitness guidance"
                                WeatherSource.LIVE -> ""
                            }
                            Text(
                                text = offlineLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
                is WeatherUiState.Error -> {
                    Text(text = state.message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Tap to retry", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f))
                }
            }
        }
    }
}

