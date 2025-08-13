package com.anever.school.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.data.model.Notice
import kotlinx.datetime.*

@Composable
fun NoticesScreen(
    onOpenDetails: (String) -> Unit
) {
    val repo = remember { Repository() }

    // Filters
    val categories = listOf("All", "Class", "Exams", "Events")
    var selectedCat by remember { mutableStateOf("All") }
    var query by remember { mutableStateOf("") }

    // Data
    var items by remember { mutableStateOf(repo.getNotices(null, null)) }
    fun refresh() {
        val cat = if (selectedCat == "All") null else selectedCat
        items = repo.getNotices(cat, query.trim())
    }

    LaunchedEffect(selectedCat, query) { refresh() }

    Column(Modifier.fillMaxSize()) {
        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { c ->
                FilterChip(
                    selected = selectedCat == c,
                    onClick = { selectedCat = c },
                    label = { Text(c) }
                )
            }
        }

        // Search
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            label = { Text("Search notices") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        // List
        val list = items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (list.isEmpty()) {
                item { Text("No notices") }
            } else {
                items(list, key = { it.notice.id }) { it ->
                    NoticeRow(
                        notice = it.notice,
                        bookmarked = it.isBookmarked,
                        onToggleBookmark = {
                            repo.toggleNoticeBookmark(it.notice.id)
                            refresh()
                        },
                        onOpen = { onOpenDetails(it.notice.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoticeRow(
    notice: Notice,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onOpen: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { Text(notice.title, fontWeight = FontWeight.SemiBold) },
            supportingContent = {
                Text("${notice.from} • ${notice.postedAt.date} ${notice.postedAt.time}")
            },
            trailingContent = {
                IconButton(onClick = onToggleBookmark) {
                    if (bookmarked) Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarked")
                    else Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark")
                }
            },
            overlineContent = { AssistChip(onClick = {}, label = { Text(notice.category) }) },
            modifier = Modifier.clickable { onOpen() }
        )
    }
}
