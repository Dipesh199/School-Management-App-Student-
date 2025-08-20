package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.data.model.BusStop
import com.anever.school.ui.design.EduCard
import com.anever.school.ui.design.EduHeroHeader
import com.anever.school.ui.design.SectionHeader
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TransportScreen() {
    val repo = remember { Repository() }
    var route by remember { mutableStateOf(repo.getTransportRoute()) } // <- keep route reactive

    val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    val currentStop: BusStop? = route.stops.firstOrNull { it.id == route.studentStopId }
    val currentEta = currentStop?.let { repo.calcEtaFor(it, now) }

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(title = "Transport", subtitle = route.name, seed = "transport")

        // --- Current selection card ---
        SectionHeader("My Pickup Stop")
        EduCard(seed = "route_current", modifier = Modifier.padding(horizontal = 16.dp)) {
            if (currentStop == null) {
                Text("No stop selected", style = MaterialTheme.typography.bodyMedium)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(currentStop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Pickup: ${currentStop.pickup} • Drop: ${currentStop.drop}", style = MaterialTheme.typography.bodySmall)
                    if (currentEta != null) {
                        Text("${currentEta.status}: ${currentEta.info}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Bus ${route.busNo} • Driver: ${route.driverName} (${route.driverPhone})", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        SectionHeader("All Stops")

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(route.stops, key = { it.id }) { s ->
                val eta = repo.calcEtaFor(s, now)
                val isCurrent = s.id == route.studentStopId

                EduCard(seed = s.name) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(s.name, fontWeight = FontWeight.SemiBold)
                            Text("Pickup: ${s.pickup} • Drop: ${s.drop}", style = MaterialTheme.typography.bodySmall)
                            Text("${eta.status}: ${eta.info}", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = {
                                // Update selection + refresh route to reflect change
                                repo.requestChangeStop(s.id)
                                route = repo.getTransportRoute()
                            },
                            enabled = !isCurrent
                        ) {
                            Text(if (isCurrent) "Selected" else "Change")
                        }
                    }
                }
            }
        }
    }
}
