
// 📁 ui/stats/StatsScreen.kt
package com.example.luontopeli.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.luontopeli.viewmodel.StatsViewModel
import com.example.luontopeli.viewmodel.formatDistance
import com.example.luontopeli.viewmodel.formatDuration
import com.example.luontopeli.viewmodel.toFormattedDate
import java.io.File

/**
 * Tilastonäkymä – kokonaistilastot, kävelyhistoria ja löydöt kuvineen ja kommentteineen.
 */
@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val sessions by viewModel.allSessions.collectAsState()
    val spots by viewModel.allSpots.collectAsState()

    val totalSteps = sessions.sumOf { it.stepCount }
    val totalDistance = sessions.sumOf { it.distanceMeters.toDouble() }.toFloat()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Tilastot",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Yhteenveto-kortit
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSummaryCard(
                    value = "$totalSteps",
                    label = "Askelta",
                    modifier = Modifier.weight(1f)
                )
                StatSummaryCard(
                    value = formatDistance(totalDistance),
                    label = "Matka",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSummaryCard(
                    value = "${spots.size}",
                    label = "Löytöjä",
                    modifier = Modifier.weight(1f)
                )
                StatSummaryCard(
                    value = "${sessions.size}",
                    label = "Lenkkejä",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Viimeisimmät löydöt, kuvat ja kommentit
        if (spots.isNotEmpty()) {
            item {
                Text(
                    "Viimeisimmät löydöt",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            items(spots) { spot ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Näytetään kuva jos se on olemassa
                        if (spot.imageLocalPath != null) {
                            AsyncImage(
                                model = File(spot.imageLocalPath),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.NaturePeople, 
                                contentDescription = null,
                                modifier = Modifier.size(70.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        
                        Spacer(Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(spot.name, style = MaterialTheme.typography.titleSmall)
                            
                            // TÄMÄ KOHTA NÄYTTÄÄ KOMMENTIN
                            if (!spot.comment.isNullOrBlank()) {
                                Text(
                                    "\"${spot.comment}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            
                            Text(
                                spot.timestamp.toFormattedDate(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Kävelyhistoria
        if (sessions.isNotEmpty()) {
            item {
                Text(
                    "Kävelyhistoria",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            items(sessions) { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DirectionsWalk, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "${session.stepCount} askelta • ${formatDistance(session.distanceMeters)}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                session.startTime.toFormattedDate(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        if (sessions.isEmpty() && spots.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ei vielä merkintöjä", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StatSummaryCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
