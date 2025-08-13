package com.anever.school.ui.screens


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.HorizontalDivider
import com.anever.school.data.local.dao.ExamSlotExt
import kotlinx.datetime.*

@Composable
fun ExamsScreen() {
    val repo = remember { Repository() }
    var tab by remember { mutableIntStateOf(0) } // 0=Schedule, 1=Results

    Column(Modifier.fillMaxSize()) {
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
    val slots = remember { repo.getUpcomingExams(10) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (slots.isEmpty()) {
            item { Text("No upcoming exams") }
        } else {
            items(slots, key = { it.subject.id }) { e -> ExamSlotCard(e) }
        }
    }
}

@Composable
private fun ExamSlotCard(e: ExamSlotExt) {
    ElevatedCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${e.examName}: ${e.subject.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${e.date} • ${e.start} - ${e.end}", style = MaterialTheme.typography.bodySmall)
            Text("Room ${e.room} • Seat ${e.seatNo}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ExamResultsTab(repo: Repository) {
    val exams = remember { repo.getExams() }
    var selectedExamId by remember { mutableStateOf(exams.firstOrNull()?.id) }

    Column(Modifier.fillMaxSize()) {
        // Exam chips
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exams, key = { it.id }) { ex ->
                FilterChip(
                    selected = selectedExamId == ex.id,
                    onClick = { selectedExamId = ex.id },
                    label = { Text(ex.name) }
                )
            }
        }

        if (selectedExamId == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No results available")
            }
            return
        }

        val results = remember(selectedExamId) { repo.getResultsForExam(selectedExamId!!) }
        val gpa = remember(results) { repo.computeGpa(results) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // GPA + sparkline
            item {
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("GPA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(String.format("%.2f", gpa), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (results.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Sparkline(values = results.map { it.marks })
                        }
                    }
                }
            }

            // Table header
            item {
                ElevatedCard {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("Subject", modifier = Modifier.weight(2f), fontWeight = FontWeight.SemiBold)
                            Text("Marks", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text("Grade", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        }
                        HorizontalDivider()
                        if (results.isEmpty()) {
                            Row(Modifier.padding(16.dp)) { Text("No results yet") }
                        } else {
                            results.forEach { r ->
                                Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                    Text(r.subject.name, modifier = Modifier.weight(2f))
                                    Text(r.marks.toString(), modifier = Modifier.weight(1f))
                                    Text(r.grade, modifier = Modifier.weight(1f))
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Sparkline(values: List<Int>, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary

    if (values.size <= 1) {
        // draw nothing or a small dot
        Canvas(modifier = modifier.fillMaxWidth().height(64.dp)) {
            if (values.isNotEmpty()) {
                val midY = size.height / 2f
                drawCircle(color = primaryColor, radius = 3.dp.toPx(), center = Offset(8.dp.toPx(), midY))
            }
        }
        return
    }
    val min = values.min()
    val max = values.max()
    Canvas(modifier = modifier.fillMaxWidth().height(64.dp).padding(top = 4.dp)) {
        val w = size.width
        val h = size.height
        val stepX = w / (values.size - 1).coerceAtLeast(1)
        fun norm(v: Int): Float =
            if (max == min) 0.5f else 1f - ((v - min).toFloat() / (max - min).toFloat())

        var prev = Offset(0f, norm(values.first()) * h)
        values.drop(1).forEachIndexed { idx, v ->
            val x = stepX * (idx + 1)
            val y = norm(v) * h
            val curr = Offset(x, y)
            drawLine(color = primaryColor, start = prev, end = curr, strokeWidth = 3f)
            prev = curr
        }
    }
}
