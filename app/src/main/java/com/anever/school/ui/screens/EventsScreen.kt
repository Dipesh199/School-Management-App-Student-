package com.anever.school.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.data.model.Event
import com.anever.school.data.model.EventPass
import kotlinx.datetime.*

@Composable
fun EventsScreen() {
    val repo = remember { Repository() }
    var tab by remember { mutableIntStateOf(0) } // 0=Browse, 1=My Passes
    var snack by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) { Snackbar(it) } }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Browse") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("My Passes") })
            }
            when (tab) {
                0 -> BrowseTab(repo) { snack = it }
                1 -> PassesTab(repo) { snack = it }
            }
            if (snack != null) {
                Snackbar(Modifier.padding(16.dp)) { Text(snack!!) }
            }
        }
    }
}

/* -------- Browse -------- */

@Composable
private fun BrowseTab(repo: Repository, onSnack: (String) -> Unit) {
    val cats = listOf("All", "Fest", "Seminar", "Workshop")
    var selectedCat by remember { mutableStateOf("All") }
    var q by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(repo.browseEvents(null, null)) }

    fun refresh() {
        val cat = if (selectedCat == "All") null else selectedCat
        items = repo.browseEvents(cat, q.trim())
    }
    LaunchedEffect(selectedCat, q) { refresh() }

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(16.dp),
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
            label = { Text("Search events") }
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (items.isEmpty()) {
                item { Text("No events") }
            } else {
                items(items, key = { it.event.id }) { row ->
                    EventCard(
                        e = row.event,
                        seatsLeft = row.seatsLeft,
                        hasPass = row.myPassId != null,
                        onRsvp = {
                            val res = repo.rsvp(row.event.id)
                            onSnack(res.getOrElse { it.message ?: "Error" })
                            refresh()
                        },
                        onCancel = {
                            val id = row.myPassId!!
                            val res = repo.cancelPass(id)
                            onSnack(res.getOrElse { it.message ?: "Error" })
                            refresh()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    e: Event,
    seatsLeft: Int,
    hasPass: Boolean,
    onRsvp: () -> Unit,
    onCancel: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (hasPass) {
                    OutlinedButton(onClick = onCancel) { Text("Cancel Pass") }
                } else {
                    Button(onClick = onRsvp, enabled = seatsLeft > 0) { Text("Get Pass") }
                }
            }
        }
    }
}

/* -------- My Passes -------- */

@Composable
private fun PassesTab(repo: Repository, onSnack: (String) -> Unit) {
    var rows by remember { mutableStateOf(repo.getMyPasses()) }
    fun refresh() { rows = repo.getMyPasses() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (rows.isEmpty()) {
            item { Text("No active passes") }
        } else {
            items(rows, key = { it.pass.id }) { row ->
                PassCard(
                    pass = row.pass,
                    event = row.event,
                    onCancel = {
                        val res = repo.cancelPass(row.pass.id)
                        onSnack(res.getOrElse { it.message ?: "Error" })
                        refresh()
                    }
                )
            }
        }
    }
}

@Composable
private fun PassCard(pass: EventPass, event: Event, onCancel: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${event.date} • ${event.start}-${event.end} • ${event.venue}", style = MaterialTheme.typography.bodySmall)
            AssistChip(onClick = {}, label = { Text("PASS") })
            // QR placeholder
            val outline = MaterialTheme.colorScheme.outline
            Canvas(Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp)) {
                val w = size.width
                val h = size.height
                // simple squares
                drawLine(color = outline, start = Offset(0f, 0f), end = Offset(w, 0f))
                drawLine(color = outline, start = Offset(0f, h), end = Offset(w, h))
                drawLine(color = outline, start = Offset(0f, 0f), end = Offset(0f, h))
                drawLine(color = outline, start = Offset(w, 0f), end = Offset(w, h))
            }
            Text(pass.code, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onCancel) { Text("Cancel Pass") }
            }
        }
    }
}
