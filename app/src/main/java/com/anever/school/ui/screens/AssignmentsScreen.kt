package com.anever.school.ui.screens


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository

@Composable
fun AssignmentsScreen(onOpenAssignment: (String) -> Unit) {
    val repo = Repository()
    val items = repo.getToDoAssignments()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(items.size) { i ->
            val a = items[i]
            ElevatedCard(onClick = { onOpenAssignment(a.id) }) {
                ListItem(headlineContent = { Text(a.title) }, supportingContent = { Text("Due ${a.dueAt.date} ${a.dueAt.time}") })
            }
        }
    }
}
