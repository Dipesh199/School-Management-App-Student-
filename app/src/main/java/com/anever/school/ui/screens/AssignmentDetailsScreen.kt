package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.ui.design.EduCard
import com.anever.school.ui.design.EduHeroHeader
import com.anever.school.ui.design.StatusChip
import com.anever.school.ui.design.TopBarLarge

@Composable
fun AssignmentDetailsScreen(assignmentId: String, onSubmit: () -> Unit = {}) {
    val repo = remember { Repository() }
    val a = remember(assignmentId) { repo.getAssignmentById(assignmentId) } ?: return

    Column(Modifier.fillMaxSize()) {
        TopBarLarge(title = "Assignment")
        EduHeroHeader(title = a.title, subtitle = "Due: ${a.dueAt.date} ${a.dueAt.time}", seed = a.title)
        Spacer(Modifier.height(8.dp))

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EduCard(seed = "meta") {
                Text("Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(a.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                StatusChip(a.status.name.uppercase(), MaterialTheme.colorScheme.primary)
            }
            if (a.attachments.isNotEmpty()) {
                EduCard(seed = "attachments") {
                    Text("Attachments", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    a.attachments.forEach { att ->
                        ListItem(
                            headlineContent = { Text(att.name) },
                            supportingContent = { Text("Tap to open (mock)") },
                            trailingContent = { TextButton(onClick = { /* open mock */ }) { Text("Open") } }
                        )
                        Divider()
                    }
                }
            }
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text(if (a.status.name.lowercase() == "submitted") "Resubmit" else "Submit") }
        }
    }
}
