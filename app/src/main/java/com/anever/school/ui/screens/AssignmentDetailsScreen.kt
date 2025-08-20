package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.data.model.AssignmentStatus
import com.anever.school.ui.design.EduHeroHeader
import com.anever.school.ui.design.EduCard
import com.anever.school.ui.design.SectionHeader
import com.anever.school.ui.design.StatusChip

@Composable
fun AssignmentDetailsScreen(assignmentId: String) {
    val repo = remember { Repository() }
    val base = remember(assignmentId) { repo.getAssignmentById(assignmentId) }

    if (base == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { Text("Assignment not found") }
        return
    }

    var status by remember { mutableStateOf(base.status) }
    var grade by remember { mutableStateOf(base.grade) }

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(
            title = base.title,
            subtitle = "Due ${base.dueAt.date} ${base.dueAt.time}",
            seed = "ass_$assignmentId"
        )

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
            val color = when (status) {
                AssignmentStatus.todo -> MaterialTheme.colorScheme.primary
                AssignmentStatus.submitted -> MaterialTheme.colorScheme.tertiary
                AssignmentStatus.graded -> MaterialTheme.colorScheme.secondary
            }
            StatusChip(status.name.uppercase(), color)
            if (grade != null) StatusChip("Grade: $grade", MaterialTheme.colorScheme.secondary)
        }

        Spacer(Modifier.height(12.dp))
        SectionHeader("Description")
        EduCard(seed = "ass_desc", modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(base.description, style = MaterialTheme.typography.bodyMedium)
        }

        if (base.attachments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionHeader("Attachments")
            EduCard(seed = "ass_atts", modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
        SectionHeader("Actions")
        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { status = AssignmentStatus.submitted }) {
                Text(if (status == AssignmentStatus.todo) "Submit" else "Resubmit")
            }
            if (status == AssignmentStatus.submitted || status == AssignmentStatus.graded) {
                OutlinedButton(onClick = { grade = "A" }) { Text("View Grade (mock)") }
            }
        }
    }
}