package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.data.model.Event
import com.anever.school.ui.design.EduCard
import com.anever.school.ui.design.EduHeroHeader
import kotlinx.coroutines.launch

@Composable
fun EventsScreen() {
    val repo = remember { Repository() }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // Tabs: Browse / My Passes
    var tab by remember { mutableStateOf(0) }

    // Browse state
    var selectedCat by remember { mutableStateOf("All") }
    var q by remember { mutableStateOf("") }
    var browse by remember { mutableStateOf(repo.browseEvents(null, null)) } // List<EventItem>

    // Passes state
    var passes by remember { mutableStateOf(repo.getMyPasses()) } // List<PassRow>

    fun refreshBrowse() {
        val cat = if (selectedCat == "All") null else selectedCat
        browse = repo.browseEvents(cat, q.trim())
    }
    fun refreshPasses() { passes = repo.getMyPasses() }
    fun refreshBoth() { refreshBrowse(); refreshPasses() }

    // initial + query/category
    LaunchedEffect(Unit) { refreshBoth() }
    LaunchedEffect(selectedCat, q) { refreshBrowse() }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            EduHeroHeader(title = "Events & Passes", subtitle = "Fests, seminars, workshops", seed = "events")

            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Browse") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("My Passes") })
            }

            if (tab == 0) {
                // --- Browse tab ---
                val cats = listOf("All", "Fest", "Seminar", "Workshop")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cats) { c ->
                        FilterChip(
                            selected = selectedCat == c,
                            onClick = { selectedCat = c },
                            label = { Text(c) }
                        )
                    }
                }
                OutlinedTextField(
                    value = q,
                    onValueChange = { q = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true,
                    label = { Text("Search events") }
                )
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (browse.isEmpty()) {
                        item { Text("No events") }
                    } else {
                        items(browse, key = { it.event.id }) { row ->
                            val e = row.event
                            val hasPass = row.myPassId != null
                            EduCard(seed = e.title) {
                                EventInfo(e = e, seatsLeft = row.seatsLeft)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    if (hasPass) {
                                        OutlinedButton(onClick = {
                                            val res = repo.cancelPass(row.myPassId!!)
                                            scope.launch { snackbar.showSnackbar(res.getOrElse { it.message ?: "Error" }) }
                                            refreshBoth()
                                        }) { Text("Cancel Pass") }
                                    } else {
                                        Button(
                                            onClick = {
                                                val res = repo.rsvp(e.id)
                                                scope.launch { snackbar.showSnackbar(res.getOrElse { it.message ?: "Error" }) }
                                                refreshBoth()
                                            },
                                            enabled = row.seatsLeft > 0
                                        ) { Text(if (row.seatsLeft > 0) "Get Pass (${row.seatsLeft})" else "Full") }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // --- My Passes tab ---
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (passes.isEmpty()) {
                        item { Text("No active passes") }
                    } else {
                        items(passes, key = { it.pass.id }) { row ->
                            val e = row.event
                            EduCard(seed = e.title) {
                                ListItem(
                                    headlineContent = { Text(e.title, fontWeight = FontWeight.SemiBold) },
                                    supportingContent = { Text("${e.date} • ${e.venue} • Code: ${row.pass.code}") },
                                    trailingContent = {
                                        Button(onClick = {
                                            val res = repo.cancelPass(row.pass.id)
                                            scope.launch { snackbar.showSnackbar(res.getOrElse { it.message ?: "Error" }) }
                                            refreshBoth()
                                        }) { Text("Cancel") }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventInfo(e: Event, seatsLeft: Int) {
    // Fields of Event come straight from your models. :contentReference[oaicite:1]{index=1}
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(e.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(e.category) })
            AssistChip(onClick = {}, label = { Text("${e.date} • ${e.start}-${e.end}") })
        }
        AssistChip(onClick = {}, label = { Text(e.venue) })
        Text(e.description, style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("Capacity: ${e.capacity}") })
            AssistChip(onClick = {}, label = { Text("Seats left: $seatsLeft") })
        }
    }
}
