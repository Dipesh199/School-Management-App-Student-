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
import com.anever.school.ui.design.*

@Composable
fun NoticesScreen(onOpenDetails: (String) -> Unit) {
    val repo = remember { Repository() }
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }

    fun items() = repo.getNotices(search.ifBlank { null }, category)
    var list by remember { mutableStateOf(items()) }
    val refresh = { list = items() }

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(title = "Notices", subtitle = "School & class announcements", seed = "notices")

        val cats = remember { listOf("All", "Class", "Exams", "Events") }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cats) { c ->
                val selected = (category == null && c == "All") || (category == c)
                FilterChip(selected = selected, onClick = {
                    category = if (c == "All") null else c
                    refresh()
                }, label = { Text(c) })
            }
        }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it; refresh() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            label = { Text("Search notices") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (list.isEmpty()) {
                item { Text("No notices") }
            } else {
                items(list, key = { it.notice.id }) { row ->
                    NoticeRow(
                        notice = row.notice,
                        bookmarked = row.isBookmarked,
                        onToggleBookmark = { repo.toggleNoticeBookmark(row.notice.id); refresh() },
                        onOpen = { onOpenDetails(row.notice.id) }
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
    EduCard(seed = notice.title, modifier = Modifier.clickable { onOpen() }) {
        ListItem(
            headlineContent = { Text(notice.title, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text("${notice.from} • ${notice.postedAt.date} ${notice.postedAt.time}") },
            trailingContent = {
                IconButton(onClick = onToggleBookmark) {
                    if (bookmarked) Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarked")
                    else Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark")
                }
            },
            overlineContent = { AssistChip(onClick = {}, label = { Text(notice.category) }) }
        )
    }
}
