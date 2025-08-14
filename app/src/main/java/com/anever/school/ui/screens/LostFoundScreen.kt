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
import com.anever.school.data.model.LostFoundItem
import androidx.compose.runtime.mutableIntStateOf

@Composable
fun LostFoundScreen() {
    val repo = remember { Repository() }
    var tab by remember { mutableIntStateOf(0) } // 0=Feed, 1=My Reports, 2=Alerts
    var snack by remember { mutableStateOf<String?>(null) }
    var dialogType by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            if (tab != 2) {
                ReportFab(
                    onLost = { dialogType = "Lost" },
                    onFound = { dialogType = "Found" }
                )
            }
        },
        snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Feed") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("My Reports") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Alerts") })
            }
            when (tab) {
                0 -> FeedTab(repo) { snack = it }
                1 -> MyReportsTab(repo) { snack = it }
                2 -> AlertsTab(repo) { snack = it }
            }
            if (snack != null) {
                Snackbar(Modifier.padding(16.dp)) { Text(snack!!) }
            }
        }

        if (dialogType != null) {
            val type = dialogType!!
            ReportDialog(
                type = type,
                onDismiss = { dialogType = null },
                onSubmit = { title, category, description, location, reward ->
                    repo.addLostOrFound(type, title, category, description, location, reward)
                    snack = "$type reported"
                    dialogType = null
                }
            )
        }
    }
}

/* ---------------- Feed ---------------- */

@Composable
private fun FeedTab(repo: Repository, onSnack: (String) -> Unit) {
    val types = listOf("All", "Lost", "Found")
    val cats = listOf("All", "Electronics", "Books", "Clothing", "ID/Docs", "Accessories", "Others")
    var type by remember { mutableStateOf("All") }
    var cat by remember { mutableStateOf("All") }
    var q by remember { mutableStateOf("") }

    var rows by remember { mutableStateOf(repo.listLostFound(null, null, null)) }
    fun refresh() {
        val t = if (type == "All") null else type
        val c = if (cat == "All") null else cat
        rows = repo.listLostFound(t, c, q)
    }
    LaunchedEffect(type, cat, q) { refresh() }

    Column(Modifier.fillMaxSize()) {
        // Type chips
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(types) { t ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
            }
        }
        // Category chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cats) { c ->
                FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) })
            }
        }
        // Search
        OutlinedTextField(
            value = q, onValueChange = { q = it },
            singleLine = true,
            label = { Text("Search location/title/description") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (rows.isEmpty()) {
                item { Text("No items") }
            } else {
                items(rows, key = { it.item.id }) { row ->
                    LostFoundCard(
                        item = row.item,
                        isMine = row.isMine,
                        onResolveMine = {
                            if (repo.markResolved(row.item.id)) onSnack("Marked resolved")
                            refresh()
                        },
                        onContact = { onSnack("Contact: ${row.item.contactName} • ${row.item.contactPhone} (mock)") }
                    )
                }
            }
        }
    }
}

/* ---------------- My Reports ---------------- */

@Composable
private fun MyReportsTab(repo: Repository, onSnack: (String) -> Unit) {
    var rows by remember { mutableStateOf(repo.listLostFound(null, null, null).filter { it.isMine }) }
    fun refresh() { rows = repo.listLostFound(null, null, null).filter { it.isMine } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (rows.isEmpty()) {
            item { Text("You have not reported anything yet.") }
        } else {
            items(rows, key = { it.item.id }) { row ->
                LostFoundCard(
                    item = row.item,
                    isMine = true,
                    onResolveMine = {
                        if (repo.markResolved(row.item.id)) onSnack("Marked resolved")
                        refresh()
                    },
                    onContact = { onSnack("You can be contacted at ${row.item.contactPhone}") }
                )
            }
        }
    }
}

/* ---------------- Alerts ---------------- */

@Composable
private fun AlertsTab(repo: Repository, onSnack: (String) -> Unit) {
    var cats by remember { mutableStateOf(repo.getAlertCategories()) }
    fun refresh() { cats = repo.getAlertCategories() }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Category Alerts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        cats.forEach { (cat, enabled) ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(cat) },
                    supportingContent = { Text("Notify me when new $cat items are posted (mock).") },
                    trailingContent = {
                        Switch(checked = enabled, onCheckedChange = {
                            val on = repo.toggleAlertCategory(cat)
                            onSnack(if (on) "Subscribed to $cat" else "Unsubscribed from $cat")
                            refresh()
                        })
                    }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { onSnack("Test alert: New item in your subscribed categories!") }) {
            Text("Send test alert (mock)")
        }
    }
}

/* ---------------- UI blocks & dialogs ---------------- */

@Composable
private fun LostFoundCard(
    item: LostFoundItem,
    isMine: Boolean,
    onResolveMine: () -> Unit,
    onContact: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(item.type) })
                AssistChip(onClick = {}, label = { Text(item.category) })
                AssistChip(onClick = {}, label = { Text(item.status) })
            }
            Text("${item.dateTime.date} • ${item.location}", style = MaterialTheme.typography.bodySmall)
            if (!item.description.isBlank()) Text(item.description, style = MaterialTheme.typography.bodySmall)
            if (item.reward != null && item.type == "Lost") {
                AssistChip(onClick = {}, label = { Text("Reward: €${item.reward}") })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (isMine && item.status == "Open") {
                    OutlinedButton(onClick = onResolveMine) { Text("Mark Resolved") }
                } else {
                    TextButton(onClick = onContact) { Text(if (item.type == "Found") "Contact Finder" else "Contact Owner") }
                }
            }
        }
    }
}

@Composable
private fun ReportFab(onLost: () -> Unit, onFound: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ExtendedFloatingActionButton(onClick = { expanded = true }) {
            Text("Report")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Report Lost") }, onClick = { expanded = false; onLost() })
            DropdownMenuItem(text = { Text("Report Found") }, onClick = { expanded = false; onFound() })
        }
    }
}

@Composable
private fun ReportDialog(
    type: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, category: String, description: String, location: String, reward: Int?) -> Unit
) {
    val cats = listOf("Electronics", "Books", "Clothing", "ID/Docs", "Accessories", "Others")
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(cats.first()) }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var rewardText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report $type") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                // category as chips
                Text("Category", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cats) { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                    }
                }
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") })
                if (type == "Lost") {
                    OutlinedTextField(
                        value = rewardText,
                        onValueChange = { rewardText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Reward (optional, €)") },
                        singleLine = true
                    )
                }
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    title.isBlank() -> error = "Title is required"
                    location.isBlank() -> error = "Location is required"
                    else -> {
                        val reward = rewardText.toIntOrNull()
                        onSubmit(title.trim(), category, description.trim(), location.trim(), reward)
                    }
                }
            }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}