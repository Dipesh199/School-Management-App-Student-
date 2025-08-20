package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.ui.design.EduHeroHeader
import com.anever.school.ui.design.EduCard
import com.anever.school.ui.design.SectionHeader

@Composable
fun ExamsScreen() {
    val repo = remember { Repository() }
    var tab by remember { mutableStateOf(0) } // 0: schedule, 1: results

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(title = "Exams & Grades", subtitle = "Schedules and results", seed = "exams")

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Schedule") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Results") })
        }

        when (tab) {
            0 -> ExamScheduleTab(repo)
            1 -> ExamResultsTab(repo)
        }
    }
}

@Composable
private fun ExamScheduleTab(repo: Repository) {
    val slots = remember { repo.getUpcomingExams() } // subject/date/time/room info
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (slots.isEmpty()) {
            item { Text("No upcoming exams") }
        } else {
            items(slots, key = { it.subject.id }) { e ->
                EduCard(seed = e.subject.name) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${e.examName}: ${e.subject.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${e.date} • ${e.start} - ${e.end}", style = MaterialTheme.typography.bodySmall)
                        Text("Room ${e.room} • Seat ${e.seatNo}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// ... keep your imports and top parts the same ...

@Composable
private fun ExamResultsTab(repo: Repository) {
    val exams = remember { repo.getExams() }
    var selectedExamId by remember { mutableStateOf(exams.firstOrNull()?.id) }

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exams, key = { it.id }) { ex ->
                FilterChip(
                    selected = selectedExamId == ex.id,
                    onClick = { selectedExamId = ex.id },
                    label = { Text(ex.term) }
                )
            }
        }

        val rows = remember(selectedExamId) { selectedExamId?.let { repo.getResultsForExam(it) } ?: emptyList() }
        val gpa = remember(rows) { repo.computeGpa(rows) }

        SectionHeader("Results")
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                EduCard(seed = "gpa") { Text("GPA: $gpa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            }
            items(rows, key = { it.subject.id }) { r ->
                EduCard(seed = r.subject.name) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(r.subject.name, fontWeight = FontWeight.SemiBold)
                        Text("${r.marks} (${r.grade})")
                    }
                }
            }
        }
    }
}

