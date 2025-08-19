package com.anever.school.ui.screens

import androidx.compose.foundation.clickable
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
import com.anever.school.data.model.Assignment
import com.anever.school.data.model.AssignmentStatus
import com.anever.school.data.model.Subject
import com.anever.school.ui.design.GradientCard

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
        FilterRow(current = statusFilter, onSelect = { statusFilter = it })
        SubjectChipsRow(subjects = subjects, selectedId = selectedSubjectId, onSelect = { selectedSubjectId = it })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filtered.isEmpty()) {
                item { Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) { Box(Modifier.padding(16.dp)) { Text("No assignments") } } }
            } else {
                items(filtered, key = { it.assignment.id }) { item ->
                    AssignmentCard(item = item) { onOpenAssignment(item.assignment.id) }
                }
            }
        }
    }
}

private enum class AssignFilter(val label: String) { TODO("To-Do"), SUBMITTED("Submitted"), GRADED("Graded"), ALL("All") }

@Composable
private fun FilterRow(current: AssignFilter, onSelect: (AssignFilter) -> Unit) {
    val filters = listOf(AssignFilter.TODO, AssignFilter.SUBMITTED, AssignFilter.GRADED, AssignFilter.ALL)
    LazyRow(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { f ->
            FilterChip(selected = current == f, onClick = { onSelect(f) }, label = { Text(f.label) })
        }
    }
}

@Composable
private fun SubjectChipsRow(subjects: List<Subject>, selectedId: String?, onSelect: (String?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { FilterChip(selected = selectedId == null, onClick = { onSelect(null) }, label = { Text("All Subjects") }) }
        items(subjects, key = { it.id }) { s ->
            FilterChip(selected = selectedId == s.id, onClick = { onSelect(s.id) }, label = { Text(s.name) })
        }
    }
}

private data class AssignmentItem(val assignment: Assignment, val subject: Subject?)

@Composable
private fun AssignmentCard(item: AssignmentItem, onClick: () -> Unit) {
    val a = item.assignment
    val subj = item.subject

    GradientCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // FIX: actually invoke onClick
    ) {
        Column(Modifier.padding(0.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(a.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subj?.name ?: "Subject: ${'$'}{a.subjectId}", style = MaterialTheme.typography.bodyMedium)
            Text("Due: ${'$'}{a.dueAt.date} ${'$'}{a.dueAt.time}", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onClick, label = { Text(a.status.name.uppercase()) })
                if (a.status == AssignmentStatus.graded && a.grade != null) {
                    AssistChip(onClick = {}, label = { Text("Grade: ${'$'}{a.grade}") })
                }
            }
        }
    }
}