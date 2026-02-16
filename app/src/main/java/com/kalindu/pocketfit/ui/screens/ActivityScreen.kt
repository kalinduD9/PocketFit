package com.kalindu.pocketfit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalindu.pocketfit.utils.SampleData

@Composable
fun ActivityScreen(
    onActivityClick: (Int) -> Unit
) {
    // Main content
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Steps
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🚶",
                                    fontSize = 32.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "11,650",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Steps",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Calories
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🔥",
                                    fontSize = 32.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "640",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "kcal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Duration
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⏱️",
                                    fontSize = 32.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "1H 30M",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Duration",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Activity Breakdown Header
            item {
                Text(
                    text = "Activity Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            // List of Activities
            items(SampleData.sampleActivities) { activity ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onActivityClick(activity.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon and activity type
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Activity icon in colored box
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = when (activity.type.lowercase()) {
                                        "walking" -> Icons.AutoMirrored.Filled.DirectionsWalk
                                        "running" -> Icons.AutoMirrored.Filled.DirectionsRun
                                        "cycling" -> Icons.AutoMirrored.Filled.DirectionsBike
                                        else -> Icons.AutoMirrored.Filled.DirectionsRun
                                    },
                                    contentDescription = activity.type,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Activity name and duration
                            Column {
                                Text(
                                    text = activity.type,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activity.duration,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Steps and calories
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${activity.steps} Steps",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${activity.calories} kcal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Extra space at bottom
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { /* Add new activity */ },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Activity"
            )
        }
    }
}