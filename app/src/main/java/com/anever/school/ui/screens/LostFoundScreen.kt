package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.ui.design.EduCard
import com.anever.school.ui.design.EduHeroHeader
import com.anever.school.ui.design.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostFoundScreen() {
    val repo = remember { Repository() }

    // Tabs: Lost, Found, Mine
    val tabs = listOf("Lost", "Found", "Mine")
    var tabIndex by remember { mutableStateOf(0) } // 0 Lost, 1 Found, 2 Mine

    // Filters
    var query by remember { mutableStateOf("") }
    var selectedCategory: String? by remember { mutableStateOf(null) }

    // Data lists
    var itemsList by remember { mutableStateOf(listOf<Repository.LFRow>()) }
    var categories by remember { mutableStateOf(listOf<String>()) }

    fun refreshLists() {
        val type = when (tabIndex) {
            0 -> "Lost"
            1 -> "Found"
            else -> null
        }
        // Base fetch + “Mine” filter
        var list = repo.listLostFound(type = type, category = selectedCategory, query = query)
        if (tabIndex == 2) list = list.filter { it.isMine }
        itemsList = list

        // Category options for current type (for filter chips)
        val baseForCats = repo.listLostFound(type = type, category = null, query = null)
        categories = baseForCats.map { it.item.category }.distinct().sorted()
    }

    LaunchedEffect(tabIndex, selectedCategory, query) { refreshLists() }

    // Alert subscriptions (separate feature, not used for filtering)
    var alertSubs by remember { mutableStateOf(repo.getAlertCategories()) }
    fun toggleAlert(cat: String) {
        repo.toggleAlertCategory(cat)
        alertSubs = repo.getAlertCategories()
    }

    // Dialog state
    var showAddLost by remember { mutableStateOf(false) }
    var showAddFound by remember { mutableStateOf(false) }
    val allCats by remember { mutableStateOf(repo.getAlertCategories().map { it.first }) }

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(title = "Lost & Found", subtitle = "Report, search, and subscribe", seed = "lostfound")

        // Tabs
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { i, t ->
                Tab(
                    selected = tabIndex == i,
                    onClick = { tabIndex = i; selectedCategory = null },
                    text = { Text(t) }
                )
            }
        }

        // Search
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search title, description, or location") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )

        // Category filter (for current tab/type)
        if (categories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        // Results
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (itemsList.isEmpty()) {
                item { Text("No items match your filters.") }
            } else {
                items(itemsList, key = { it.item.id }) { row ->
                    val it = row.item
                    EduCard(seed = it.title) {
                        ListItem(
                            headlineContent = { Text(it.title, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("${it.type} • ${it.category} • ${it.dateTime.date} ${it.dateTime.time} @ ${it.location}") },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (row.isMine && it.status != "Resolved") {
                                        OutlinedButton(onClick = {
                                            repo.markResolved(it.id)
                                            refreshLists()
                                        }) { Text("Resolve") }
                                    }
                                }
                            }
                        )
                        if (it.description.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(it.description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Contact: ${it.contactName} • ${it.contactPhone}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                        if (it.type == "Lost" && it.reward != null) {
                            Text("Reward: ₹${it.reward}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // Alert subscriptions (independent of filters)
        SectionHeader("Alert Subscriptions")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(alertSubs, key = { it.first }) { (name, enabled) ->
                FilterChip(
                    selected = enabled,
                    onClick = { toggleAlert(name) },
                    label = { Text(name) }
                )
            }
        }

        // Add actions
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { showAddLost = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add Lost") }
            )
            ExtendedFloatingActionButton(
                onClick = { showAddFound = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add Found") }
            )
        }
    }

    if (showAddLost) {
        AddLfDialog(
            title = "Report Lost Item",
            type = "Lost",
            categoryOptions = allCats,
            onDismiss = { showAddLost = false },
            onSubmit = { title, cat, desc, loc, reward ->
                repo.addLostOrFound(
                    type = "Lost",
                    title = title,
                    category = cat,
                    description = desc,
                    location = loc,
                    reward = reward
                )
                showAddLost = false
                tabIndex = 0
                selectedCategory = null
                query = ""
            }
        )
    }

    if (showAddFound) {
        AddLfDialog(
            title = "Report Found Item",
            type = "Found",
            categoryOptions = allCats,
            onDismiss = { showAddFound = false },
            onSubmit = { title, cat, desc, loc, _ ->
                repo.addLostOrFound(
                    type = "Found",
                    title = title,
                    category = cat,
                    description = desc,
                    location = loc,
                    reward = null
                )
                showAddFound = false
                tabIndex = 1
                selectedCategory = null
                query = ""
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLfDialog(
    title: String,
    type: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, category: String, description: String, location: String, reward: Int?) -> Unit,
    categoryOptions: List<String> = emptyList()
) {
    var t by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf(categoryOptions.firstOrNull() ?: "") }
    var desc by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    var rewardText by remember { mutableStateOf("") }
    var catExpanded by remember { mutableStateOf(false) }

    val requireReward = type == "Lost"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val reward = rewardText.toIntOrNull()
                    onSubmit(t.trim(), cat.trim(), desc.trim(), loc.trim(), if (requireReward) reward else null)
                },
                enabled = t.isNotBlank() && cat.isNotBlank() && loc.isNotBlank()
            ) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = t, onValueChange = { t = it }, label = { Text("Title") }, singleLine = true)

                // Category dropdown
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = !catExpanded }) {
                    OutlinedTextField(
                        value = cat,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        categoryOptions.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = {
                                cat = opt
                                catExpanded = false
                            })
                        }
                    }
                }

                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description (optional)") })
                OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Location") }, singleLine = true)
                if (requireReward) {
                    OutlinedTextField(
                        value = rewardText,
                        onValueChange = { rewardText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Reward (₹, optional)") },
                        singleLine = true
                    )
                }
                Text("Contact will be saved as 'Me' (+91 9151 0000)", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}
