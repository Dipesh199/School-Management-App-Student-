package com.anever.school.ui.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import com.anever.school.data.model.Assignment
import com.anever.school.data.model.AssignmentStatus
import com.anever.school.data.model.Subject
import com.anever.school.ui.design.GradientCard
import kotlinx.datetime.*

@Composable
fun AssignmentsScreen(onOpenAssignment: (String) -> Unit) {
    val repo = remember { Repository() }

    // Data
    val assignments = remember { repo.getAllAssignments() }
    val subjects = remember { repo.getAllSubjects() }
    val subjectMap = remember(subjects) { subjects.associateBy { it.id } }

    // Filters
    var statusFilter by remember { mutableStateOf(AssignFilter.TODO) }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) } // null => All subjects

    // Apply filters + sort by due date
    val filtered = remember(assignments, statusFilter, selectedSubjectId) {
        assignments.asSequence()
            .filter { a ->
                when (statusFilter) {
                    AssignFilter.ALL -> true
                    AssignFilter.TODO -> a.status == AssignmentStatus.todo
                    AssignFilter.SUBMITTED -> a.status == AssignmentStatus.submitted
                    AssignFilter.GRADED -> a.status == AssignmentStatus.graded
                }
            }
            .filter { a -> selectedSubjectId == null || a.subjectId == selectedSubjectId }
            .sortedBy { it.dueAt }
            .map { a -> AssignmentItem(a, subjectMap[a.subjectId]) }
            .toList()
    }

    Column(Modifier.fillMaxSize()) {
        // Status filter row
        FilterRow(
            current = statusFilter,
            onSelect = { statusFilter = it }
        )
        // Subject filter row
        SubjectChipsRow(
            subjects = subjects,
            selectedId = selectedSubjectId,
            onSelect = { selectedSubjectId = it }
        )

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    EmptyState("No assignments")
                }
            } else {
                items(
                    items = filtered,
                    key = { it.assignment.id }
                ) { item ->
                    AssignmentCard(
                        item = item,
                        onClick = { onOpenAssignment(item.assignment.id) }
                    )
                }
            }
        }
    }
}

// ---------- UI building blocks ----------

private enum class AssignFilter(val label: String) {
    TODO("To-Do"), SUBMITTED("Submitted"), GRADED("Graded"), ALL("All")
}

@Composable
private fun FilterRow(current: AssignFilter, onSelect: (AssignFilter) -> Unit) {
    val filters = listOf(
        AssignFilter.TODO, AssignFilter.SUBMITTED, AssignFilter.GRADED, AssignFilter.ALL
    )
    LazyRow(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { f ->
            FilterChip(
                selected = current == f,
                onClick = { onSelect(f) },
                label = { Text(f.label) }
            )
        }
    }
}

@Composable
private fun SubjectChipsRow(
    subjects: List<Subject>,
    selectedId: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text("All Subjects") }
            )
        }
        items(subjects, key = { it.id }) { s ->
            FilterChip(
                selected = selectedId == s.id,
                onClick = { onSelect(s.id) },
                label = { Text(s.name) }
            )
        }
    }
}

private data class AssignmentItem(val assignment: Assignment, val subject: Subject?)

@Composable
private fun AssignmentCard(item: AssignmentItem, onClick: () -> Unit) {
    val a = item.assignment
    val subj = item.subject
    val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }

    val isLate = a.dueAt < now
    val dueText = buildString {
        append("Due: ${a.dueAt.date} ${a.dueAt.time}")
        if (isLate) append(" • LATE")
    }

    GradientCard(
        modifier = Modifier.fillMaxWidth().clickable{ onClick }
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(a.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subj?.name ?: "Subject: ${a.subjectId}", style = MaterialTheme.typography.bodyMedium)
            Text(dueText, style = MaterialTheme.typography.bodySmall, color = if (isLate) MaterialTheme.colorScheme.error else LocalContentColor.current)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onClick, label = { Text(a.status.name.uppercase()) })
                if (a.status == AssignmentStatus.graded && a.grade != null) {
                    AssistChip(onClick = {}, label = { Text("Grade: ${a.grade}") })
                }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.padding(16.dp)) {
            Text(text)
        }
    }
}

