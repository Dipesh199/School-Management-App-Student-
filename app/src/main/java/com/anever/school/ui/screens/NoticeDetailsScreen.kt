package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.data.model.Notice
import com.anever.school.ui.design.EduHeroHeader
import com.anever.school.ui.design.EduCard

@Composable
fun NoticeDetailsScreen(noticeId: String, onBack: () -> Unit = {}) {
    val repo = remember { Repository() }
    val base: Notice? = remember(noticeId) { repo.getNoticeById(noticeId) }

    if (base == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { Text("Notice not found") }
        return
    }

    var bookmarked by remember {
        mutableStateOf(repo.getNotices(null, null).firstOrNull { it.notice.id == noticeId }?.isBookmarked == true)
    }

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(
            title = base.title,
            subtitle = "${base.from} • ${base.postedAt.date} ${base.postedAt.time}",
            seed = "notice_$noticeId"
        )

        EduCard(seed = "notice_body", modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(base.body, style = MaterialTheme.typography.bodyMedium)
        }

        if (base.attachments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            EduCard(seed = "notice_atts", modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Attachments", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    base.attachments.forEach { att ->
                        ListItem(
                            headlineContent = { Text(att.name) },
                            supportingContent = { Text("Tap to open (mock)") },
                            trailingContent = { TextButton(onClick = { /* open mock */ }) { Text("Open") } }
                        )
                        Divider()
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.padding(horizontal = 16.dp)) {
            FilledIconButton(onClick = { bookmarked = repo.toggleNoticeBookmark(base.id) }) {
                if (bookmarked) Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarked")
                else Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark")
            }
            Spacer(Modifier.width(8.dp))
            Text(if (bookmarked) "Bookmarked" else "Tap to bookmark", modifier = Modifier.alignByBaseline())
        }
    }
}
