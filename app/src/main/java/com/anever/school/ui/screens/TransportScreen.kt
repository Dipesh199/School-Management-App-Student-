package com.anever.school.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.data.model.BusStop
import kotlinx.datetime.*

@Composable
fun TransportScreen() {
    val repo = remember { Repository() }
    var route by remember { mutableStateOf(repo.getTransportRoute()) }
    val tz = remember { TimeZone.currentSystemDefault() }
    val now = remember { Clock.System.now().toLocalDateTime(tz) }

    val currentStop = route.stops.first { it.id == route.studentStopId }
    val eta = remember(route.studentStopId, now) { repo.calcEtaFor(currentStop, now) }

    var pendingStopId by remember { mutableStateOf(route.studentStopId) }
    var showSnack by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = remember { SnackbarHostState() }) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Transport", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            // Route & Driver
            item {
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${route.name} • Bus ${route.busNo}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Driver: ${route.driverName}")
                        Text("Phone: ${route.driverPhone}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { /* mock call */ }) { Text("Call (mock)") }
                            OutlinedButton(onClick = { /* mock sms */ }) { Text("Message (mock)") }
                        }
                    }
                }
            }

            // Current stop + ETA
            item {
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Your Stop", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("${currentStop.name}")
                        Text("Pickup: ${currentStop.pickup}  •  Drop: ${currentStop.drop}", style = MaterialTheme.typography.bodyMedium)
                        AssistChip(onClick = {}, label = { Text(eta.status) })
                        Text(eta.info, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Map snapshot (mock)
//            item {
//                ElevatedCard {
//                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                        Text("Route Map (snapshot)", fontWeight = FontWeight.SemiBold)
//                        val outlineColor = MaterialTheme.colorScheme.outline
//                        val primaryColor = MaterialTheme.colorScheme.primary
//                        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
//                            // simple schematic: polyline with stops as dots
//                            val w = size.width
//                            val h = size.height
//                            val gap = w / (route.stops.size - 1).coerceAtLeast(1)
//                            var prev = Offset(0f, h * 0.6f)
//                            route.stops.forEachIndexed { i, _ ->
//                                val x = gap * i
//                                val y = h * 0.6f
//                                val curr = Offset(x, y)
//                                if (i > 0) drawLine(color = outlineColor, start = prev, end = curr, strokeWidth = 6f)
//                                drawCircle(color = primaryColor, radius = 8f, center = curr)
//                                prev = curr
//                            }
//                        }
//                        Text("Schematic only (no live GPS).", style = MaterialTheme.typography.bodySmall)
//                    }
//                }
//            }

            // Choose stop
            item {
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Change Stop", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        StopPicker(
                            stops = route.stops,
                            selectedId = pendingStopId,
                            onChange = { pendingStopId = it }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    if (pendingStopId != route.studentStopId) {
                                        repo.requestChangeStop(pendingStopId)
                                        route = repo.getTransportRoute() // refresh local state
                                        showSnack = "Stop change requested: ${route.stops.first { it.id == pendingStopId }.name}"
                                    }
                                },
                                enabled = pendingStopId != route.studentStopId
                            ) { Text("Request Change") }
                        }
                    }
                }
            }

            if (showSnack != null) {
                item {
                    Snackbar { Text(showSnack!!) }
                }
            }
        }
    }
}

@Composable
private fun StopPicker(
    stops: List<BusStop>,
    selectedId: String,
    onChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        stops.forEach { s ->
            ElevatedCard(
                onClick = { onChange(s.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text(s.name, fontWeight = if (s.id == selectedId) FontWeight.SemiBold else FontWeight.Normal) },
                    supportingContent = { Text("Pickup ${s.pickup} • Drop ${s.drop}") },
                    trailingContent = {
                        RadioButton(selected = s.id == selectedId, onClick = { onChange(s.id) })
                    }
                )
            }
        }
    }
}
