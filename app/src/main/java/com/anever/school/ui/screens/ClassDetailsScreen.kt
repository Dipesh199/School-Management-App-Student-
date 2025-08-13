package com.anever.school.ui.screens


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository

@Composable
fun ClassDetailsScreen(classId: String) {
    val repo = remember { Repository() }
    val subject = repo.getSubjectById(classId)
    Column(Modifier.padding(16.dp)) {
        Text("Class Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Divider(Modifier.padding(vertical = 12.dp))
        if (subject == null) {
            Text("Subject not found")
            return@Column
        }
        ListItem(
            headlineContent = { Text(subject.name) },
            supportingContent = { Text("${subject.code} • Room ${subject.room}") },
            overlineContent = { Text("Teacher: ${subject.teacher.name}") }
        )
        Text("Overview: Next session details & syllabus (dummy).")
        Text("Materials: notes/slides (dummy).")
        Text("Assignments: filter by status (dummy).")
        Text("Attendance: % and last 10 sessions (dummy).")
    }
}
