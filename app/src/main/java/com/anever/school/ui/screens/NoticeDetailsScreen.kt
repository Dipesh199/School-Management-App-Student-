package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeDetailsScreen(noticeId: String, onBack: () -> Unit = {}) {
    val repo = remember { Repository() }
    val base: Notice? = remember(noticeId) { repo.getNoticeById(noticeId) }

    if (base == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Notice not found")
        }
        return
    }

    var bookmarked by remember { mutableStateOf(repo.getNotices(null, null).firstOrNull { it.notice.id == noticeId }?.isBookmarked == true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notice") },
                navigationIcon = { /* add back if you use a nested host */ },
                actions = {
                    IconButton(onClick = {
                        bookmarked = repo.toggleNoticeBookmark(base.id)
                    }) {
                        if (bookmarked) Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarked")
                        else Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark")
                    }
                }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(base.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            AssistChip(onClick = {}, label = { Text(base.category) })
            Text("${base.from} • ${base.postedAt.date} ${base.postedAt.time}", style = MaterialTheme.typography.bodySmall)
            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(base.body, style = MaterialTheme.typography.bodyMedium)
                    if (base.attachments.isNotEmpty()) {
                        Divider()
                        Text("Attachments", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        base.attachments.forEach { att ->
                            ListItem(
                                headlineContent = { Text(att.name) },
                                supportingContent = { Text("Tap to open (mock)") },
                                trailingContent = { TextButton(onClick = { /* open mock */ }) { Text("Open") } }
                            )
                        }
                    }
                }
            }
        }
    }
}
