package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.ui.design.EduHeroHeader
import com.anever.school.ui.design.SectionHeader

@Composable
fun ClassDetailsScreen(classId: String) {
    val repo = remember { Repository() }
    val subject = remember(classId) { repo.getSubjectById(classId) }

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(
            title = subject?.name ?: "Class",
            subtitle = subject?.code ?: "",
            seed = "class_$classId"
        )

        if (subject == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Subject not found")
            }
            return
        }

        SectionHeader("Overview")

        ElevatedCard(Modifier.padding(horizontal = 16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${subject.name} • ${subject.code}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Teacher: ${subject.teacher.name}", style = MaterialTheme.typography.bodySmall)
                Text("Room: ${subject.room}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("Materials")
        ElevatedCard(Modifier.padding(horizontal = 16.dp)) { Column(Modifier.padding(16.dp)) { Text("Slides, notes, and links (dummy).") } }

        Spacer(Modifier.height(16.dp))
        SectionHeader("Assignments")
        ElevatedCard(Modifier.padding(horizontal = 16.dp)) { Column(Modifier.padding(16.dp)) { Text("Assignments for this subject (dummy).") } }

        Spacer(Modifier.height(16.dp))
        SectionHeader("Attendance")
        ElevatedCard(Modifier.padding(horizontal = 16.dp)) { Column(Modifier.padding(16.dp)) { Text("Subject attendance % and recent sessions (dummy).") } }
    }
}
