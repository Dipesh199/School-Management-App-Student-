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
import com.anever.school.data.model.AssignmentStatus
import com.anever.school.data.model.Subject
import com.anever.school.ui.design.*

@Composable
fun AssignmentsScreen(
    onOpenAssignment: (String) -> Unit
) {
    val repo = remember { Repository() }
    val all = remember { repo.getAllAssignments() }
    val subjects = remember { repo.getAllSubjects() }
    val subjectMap = remember(subjects) { subjects.associateBy { it.id } }

    var filter by remember { mutableStateOf(Filter.ToDo) }
    var subjectId by remember { mutableStateOf<String?>(null) }

    val filtered = remember(all, filter, subjectId) {
        all.filter { a ->
            (subjectId == null || a.subjectId == subjectId) &&
                    when (filter) {
                        Filter.All -> true
                        Filter.ToDo -> a.status == AssignmentStatus.todo
                        Filter.Submitted -> a.status == AssignmentStatus.submitted
                        Filter.Graded -> a.status == AssignmentStatus.graded
                    }
        }
    }

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(
            title = "Assignments",
            subtitle = "Track homework and submissions",
            seed = "assignments"
        )

        FiltersRow(filter, onChange = { filter = it })
        SubjectsRow(subjects, subjectId) { subjectId = it }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { a ->
                val subj = subjectMap[a.subjectId]
                EduCard(seed = subj?.name ?: a.title, modifier = Modifier.clickable { onOpenAssignment(a.id) }) {
                    Text(a.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(subj?.name ?: "Subject: ${a.subjectId}", style = MaterialTheme.typography.bodySmall)
                    Text("Due: ${a.dueAt.date} ${a.dueAt.time}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    val color = when (a.status) {
                        AssignmentStatus.todo -> MaterialTheme.colorScheme.primary
                        AssignmentStatus.submitted -> MaterialTheme.colorScheme.tertiary
                        AssignmentStatus.graded -> MaterialTheme.colorScheme.secondary
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(a.status.name.uppercase(), color)
                        if (a.status == AssignmentStatus.graded && a.grade != null) {
                            StatusChip("Grade: ${a.grade}", MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

private enum class Filter(val label: String) { ToDo("To-Do"), Submitted("Submitted"), Graded("Graded"), All("All") }

@Composable
private fun FiltersRow(current: Filter, onChange: (Filter) -> Unit) {
    val items = listOf(Filter.ToDo, Filter.Submitted, Filter.Graded, Filter.All)
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { it -> FilterChip(selected = current == it, onClick = { onChange(it) }, label = { Text(it.label) }) }
    }
}

@Composable
private fun SubjectsRow(subjects: List<Subject>, selected: String?, onSelect: (String?) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All Subjects") }) }
        items(subjects, key = { it.id }) { s ->
            FilterChip(selected = selected == s.id, onClick = { onSelect(s.id) }, label = { Text(s.name) })
        }
    }
}
