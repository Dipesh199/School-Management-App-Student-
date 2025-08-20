package com.anever.school.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.ui.design.EduCard
import com.anever.school.ui.design.EduHeroHeader
import com.anever.school.ui.design.SectionHeader
import com.anever.school.ui.design.StatusChip

@Composable
fun ClassesScreen(
    onOpenClass: (String) -> Unit
) {
    val repo = remember { Repository() }
    val classes = remember { repo.getTodayClasses() } // id, subject, time, room, teacher

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(
            title = "Today’s Classes",
            subtitle = if (classes.isEmpty()) "No classes today" else "You’ve got ${classes.size} class(es)",
            seed = "classes"
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (classes.isEmpty()) {
                item { Text("No classes today", modifier = Modifier.padding(16.dp)) }
            } else {
                items(classes, key = { it.id }) { c ->
                    EduCard(seed = c.subject, modifier = Modifier.clickable { onOpenClass(c.id) }) {
                        Text(c.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("${c.time} • Room ${c.room}", style = MaterialTheme.typography.bodySmall)
                        Text("Teacher: ${c.teacher}", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        StatusChip("Next", MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
