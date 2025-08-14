package com.anever.school.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.data.TodayClass
import com.anever.school.data.local.dao.ExamSlotExt
import com.anever.school.data.model.Assignment
import com.anever.school.ui.design.GradientCard
import com.anever.school.ui.design.HeroHeader
import com.anever.school.ui.design.StatPill
import kotlinx.datetime.*

@Composable
fun HomeScreen(
    onOpenClass: (String) -> Unit,
    onOpenAssignment: (String) -> Unit,
    onOpenExamSchedule: () -> Unit,
    onOpenNotices: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenLostFound: () -> Unit
) {
    val repo = remember { Repository() }
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val classes = remember { repo.getTodayClasses(today) }
    val assignments = remember { repo.getToDoAssignments() }
    val exams = remember { repo.getUpcomingExams(3) }
    val notices = remember { repo.getLatestNotices(3) }
    val events = remember { repo.getUpcomingEvents(3) }
    val latestLF = remember { repo.latestLostFound(3) }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroHeader(
                title = "Hi, Student 👋",
                subtitle = "Here’s your day: classes, assignments & news"
            )
        }

        item {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill("Attendance ${/* e.g. */ "82%"}")
                StatPill("Due: ${/* e.g. */ "2"}", color = MaterialTheme.colorScheme.tertiary)
                StatPill("Next exam ${/* e.g. */ "Sept 3"}", color = MaterialTheme.colorScheme.secondary)
            }
        }

        item {

            Row {
                Text(
                    "Today : ${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${today.dayOfMonth} ${today.year}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Today’s Classes
        item { SectionHeader("Today’s classes") }
        if (classes.isEmpty()) {
            item { EmptyState("No classes today") }
        } else {
            items(classes) { c -> TodayClassCard(c, onClick = { onOpenClass(c.id) }) }
        }

        // Due Assignments
        item { SectionHeader("Due assignments") }
        if (assignments.isEmpty()) {
            item { EmptyState("All caught up") }
        } else {
            items(assignments) { a -> AssignmentCard(a, onClick = { onOpenAssignment(a.id) }) }
        }

        // Upcoming Exams
        item { SectionHeader("Upcoming exams") }
        if (exams.isEmpty()) {
            item { EmptyState("No upcoming exams") }
        } else {
            items(exams) { e -> ExamSlotCard(e) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onOpenExamSchedule) {
                        Text("See full schedule")
                    }
                }
            }
        }

        // Latest Notices
        item { SectionHeader("Latest notices") }
        if (notices.isEmpty()) {
            item { EmptyState("No notices") }
        } else {
            items(notices) { n ->
                ListItem(
                    headlineContent = { Text(n.title) },
                    supportingContent = { Text("${n.from} • ${n.postedAt.date} ${n.postedAt.time}") }
                )
                Divider()
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onOpenNotices) {
                        Text("Open Notices")
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
        // Upcoming Events
        item { SectionHeader("Upcoming events") }
        if (events.isEmpty()) {
            item { EmptyState("No upcoming events") }
        } else {
            items(events) { ev ->
                GradientCard {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            ev.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${ev.date} • ${ev.start}-${ev.end} • ${ev.venue}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onOpenEvents) { Text("View all events") }
                }
            }
        }

        // Lost & Found (latest)
        item { SectionHeader("Lost & Found") }
        if (latestLF.isEmpty()) {
            item { EmptyState("No reports yet") }
        } else {
            items(latestLF, key = { it.id }) { lf ->
                GradientCard {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            lf.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text(lf.type) })
                            AssistChip(onClick = {}, label = { Text(lf.category) })
                            AssistChip(onClick = {}, label = { Text(lf.status) })
                        }
                        Text(
                            "${lf.dateTime.date} • ${lf.location}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onOpenLostFound) { Text("Open Lost & Found") }
                }
            }
        }

    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun EmptyState(text: String) {
    AssistChip(onClick = {}, label = { Text(text) })
}

@Composable
private fun TodayClassCard(c: TodayClass, onClick: () -> Unit) {
    GradientCard( modifier = Modifier.clickable{ onClick()}) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    c.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("${c.time} • Room ${c.room}", style = MaterialTheme.typography.bodyMedium)
                Text("Teacher: ${c.teacher}", style = MaterialTheme.typography.bodySmall)
            }
    }

}

@Composable
private fun AssignmentCard(a: Assignment, onClick: () -> Unit) {
    GradientCard( modifier = Modifier.clickable{ onClick()}) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                a.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("Due: ${a.dueAt.date} ${a.dueAt.time}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Subject: ${a.subjectId}",
                style = MaterialTheme.typography.bodySmall
            ) // kept simple
            AssistChip(onClick = onClick, label = { Text(a.status.name.uppercase()) })
        }
    }
}

@Composable
private fun ExamSlotCard(e: ExamSlotExt) {
    GradientCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "${e.examName}: ${e.subject.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("${e.date} • ${e.start} - ${e.end}", style = MaterialTheme.typography.bodySmall)
            Text("Room ${e.room} • Seat ${e.seatNo}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
