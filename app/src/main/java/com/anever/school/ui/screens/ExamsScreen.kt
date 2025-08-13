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
fun ExamsScreen() {
    val repo = Repository()
    val slots = repo.getUpcomingExams(10)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(slots.size) { i ->
            val e = slots[i]
            ElevatedCard {
                ListItem(
                    headlineContent = { Text("${e.subject.name} • ${e.date}") },
                    supportingContent = { Text("${e.start} - ${e.end} • Room ${e.room} • Seat ${e.seatNo}") }
                )
            }
        }
    }
}
