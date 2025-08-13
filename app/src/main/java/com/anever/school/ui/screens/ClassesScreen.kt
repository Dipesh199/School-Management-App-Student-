package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository

@Composable
fun ClassesScreen(onOpenClass: (String) -> Unit) {
    val repo = Repository()
    val subjects = repo.getUpcomingExams().map { it.subject }.distinctBy { it.id } +
            repo.getSubjectById("s1")!! // ensure at least one
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(subjects.size) { i ->
            val s = subjects[i]
            ElevatedCard(onClick = { onOpenClass(s.id) }) {
                ListItem(headlineContent = { Text(s.name) }, supportingContent = { Text("${s.code} • Room ${s.room}") })
            }
        }
    }
}
