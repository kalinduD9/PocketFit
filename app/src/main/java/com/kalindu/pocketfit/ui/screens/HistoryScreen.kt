package com.kalindu.pocketfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalindu.pocketfit.utils.SampleData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    // Track which filter is selected
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Walking", "Running", "Cycling")

    // Filter activities based on selected filter
    val filteredActivities = if (selectedFilter == "All") {
        SampleData.sampleActivities
    } else {
        SampleData.sampleActivities.filter { it.type == selectedFilter }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Filter Chips Section
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterOptions) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    leadingIcon = if (selectedFilter == filter) {
                        {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }

        // Activities List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group activities by date
            val groupedActivities = filteredActivities.groupBy { it.date }

            groupedActivities.forEach { (date, activitiesOnDate) ->
                // Date Header
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }

                // Daily Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Total steps for the day
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🚶",
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = activitiesOnDate.sumOf { it.steps }.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Steps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            // Total calories for the day
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🔥",
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = activitiesOnDate.sumOf { it.calories }.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Calories",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            // Number of activities
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📊",
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = activitiesOnDate.size.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Activities",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Individual Activity Cards for that date
                items(activitiesOnDate) { activity ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left side: Icon and activity info
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Activity icon
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(48.dp)
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
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Activity details
                                Column {
                                    Text(
                                        text = activity.type,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${activity.time} • ${activity.duration}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Right side: Distance and calories
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = activity.distance,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${activity.calories} kcal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
