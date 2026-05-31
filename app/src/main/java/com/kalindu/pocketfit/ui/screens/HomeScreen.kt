package com.kalindu.pocketfit.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kalindu.pocketfit.ui.viewmodel.AuthViewModel
import com.kalindu.pocketfit.ui.viewmodel.HomeViewModel
import com.kalindu.pocketfit.ui.viewmodel.WeatherUiState
import com.kalindu.pocketfit.utils.LocationHelper
import com.kalindu.pocketfit.utils.SampleData

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val currentSteps by homeViewModel.currentSteps
    val isSensorAvailable by homeViewModel.isStepSensorAvailable

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
                    homeViewModel.setWeatherError("Unable to retrieve location using GPS. Tap to retry.")
                }
            }
        }
    }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            homeViewModel.startStepTracking(context)
        }
    }

    // Initialize fetching and tracking
    LaunchedEffect(Unit) {
        // Handle Weather
        val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                          ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasLocation) {
            locationHelper.getCurrentLocation { location ->
                if (location != null) homeViewModel.getWeatherForLocation(location.latitude, location.longitude)
            }
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }

        // Handle Steps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                homeViewModel.startStepTracking(context)
            } else {
                activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        } else {
            homeViewModel.startStepTracking(context)
        }
    }

    val scrollState = rememberScrollState()
    val stepsProgress = currentSteps / 10000f
    val caloriesProgress = SampleData.todayCalories / 1000f
    val weatherState by homeViewModel.weatherState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = "Welcome Back, ${authViewModel.currentUserName}!",
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
                if (loc != null) homeViewModel.getWeatherForLocation(loc.latitude, loc.longitude)
            }
        })

        // Steps Today Card
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
                            text = if (isSensorAvailable) currentSteps.toString() else "--",
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

                if (!isSensorAvailable) {
                    Text(
                        text = "Step sensor not available on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                val goalSteps = 10000
                Text(
                    text = "Goal: $goalSteps steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        // Calories Burned Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Calories Burned", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "🔥", fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = SampleData.todayCalories.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { caloriesProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${(caloriesProgress * 100).toInt()}% of daily goal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Current Activity Card
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Current Activity", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(bottom = 4.dp))
                    Text(text = SampleData.currentActivity, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Finish")
                }
            }
        }

        // Quick Stats Summary
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(text = "Today's Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(label = "Distance", value = "2.4 km", icon = "🏃")
                    StatItem(label = "Active Time", value = "45 min", icon = "⏱️")
                    StatItem(label = "Activities", value = "2", icon = "📊")
                }
            }
        }
    }
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
                    val weather = state.weather.weather.firstOrNull()
                    val temp = state.weather.main.temp.toInt()
                    val city = state.weather.cityName
                    val (status, tip, icon) = when {
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
                                Text(text = "$status in $city", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                Text(text = "$temp°C", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f), shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                            Text(text = tip, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onTertiaryContainer, textAlign = TextAlign.Center, modifier = Modifier.padding(8.dp))
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

@Composable
fun StatItem(label: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = icon, fontSize = 24.sp, modifier = Modifier.padding(bottom = 4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
