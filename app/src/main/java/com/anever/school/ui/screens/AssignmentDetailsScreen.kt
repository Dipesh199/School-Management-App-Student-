package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository

@Composable
fun AssignmentDetailsScreen(assignmentId: String) {
    val repo = remember { Repository() }
    val a = repo.getAssignmentById(assignmentId)
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Assignment Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (a == null) {
            Text("Assignment not found")
            return@Column
        }
        Text(a.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Subject: ${a.subjectId}")
        Text("Due: ${a.dueAt.date} ${a.dueAt.time}")
        Text("Status: ${a.status}")
        Text("Description: ${a.description}")
        if (a.attachments.isNotEmpty()) {
            Text("Attachments:")
            a.attachments.forEach { Text("• ${it.name}") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /* submit (mock) */ }) { Text("Submit") }
            OutlinedButton(onClick = { /* resubmit (mock) */ }) { Text("Resubmit") }
        }
    }
}
