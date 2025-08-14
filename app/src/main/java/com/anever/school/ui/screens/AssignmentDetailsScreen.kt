package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import com.anever.school.data.model.AssignmentStatus
import com.anever.school.ui.design.GradientCard
import kotlinx.coroutines.delay
import kotlinx.datetime.*


@Composable
fun AssignmentDetailsScreen(assignmentId: String) {
    val repo = remember { Repository() }
    val original = remember(assignmentId) { repo.getAssignmentById(assignmentId) }

    if (original == null) {
        MissingAssignmentView()
        return
    }

    // ----- Local (mock) state for this screen session -----
    var assignment by remember(assignmentId) {
        mutableStateOf(original)
    }
    var submitNote by rememberSaveable { mutableStateOf<String?>(null) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    val subject = remember(assignment.subjectId) { repo.getSubjectById(assignment.subjectId) }

    // Live countdown (updates each second)
    val now by tickingNow()
    val dueInstant = remember(assignment.dueAt) {
        assignment.dueAt.toInstant(TimeZone.currentSystemDefault())
    }
    val remaining = dueInstant - now
    val isOverdue = remaining.isNegative()
    val remainingText = formatDuration(remaining)

    val lateAndUnsubmitted =
        isOverdue && (assignment.status == AssignmentStatus.todo)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = assignment.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionChip(
                onClick = {},
                label = { Text(assignment.status.name.uppercase()) }
            )
            if (lateAndUnsubmitted) {
                AssistChip(
                    onClick = {},
                    label = { Text("LATE") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        }

        // Meta
        GradientCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Subject & Teacher", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = buildString {
                        append(subject?.name ?: "Subject: ${assignment.subjectId}")
                        subject?.teacher?.let { append("  •  ${it.name}") }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                HorizontalDivider()
                Text("Due", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${assignment.dueAt.date} ${assignment.dueAt.time}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (isOverdue) "Overdue by $remainingText" else "Time remaining: $remainingText",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else LocalContentColor.current
                )
            }
        }

        // Description
        GradientCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(assignment.description, style = MaterialTheme.typography.bodyMedium)
                if (assignment.attachments.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Attachments", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    assignment.attachments.forEach { att ->
                        ListItem(
                            headlineContent = { Text(att.name) },
                            supportingContent = { Text("Tap to open (mock)") },
                            trailingContent = {
                                TextButton(onClick = { /* mock open viewer */ }) { Text("Open") }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        // Grade / Feedback (dummy display)
        GradientCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Grade & Feedback", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (assignment.status == AssignmentStatus.graded) {
                    Text("Grade: ${assignment.grade ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Feedback: ${assignment.feedback ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Pending grading.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (assignment.status) {
                AssignmentStatus.todo -> {
                    Button(onClick = { showSubmitDialog = true }, enabled = true) {
                        Text("Submit")
                    }
                    OutlinedButton(onClick = { /* noop */ }, enabled = false) {
                        Text("Resubmit")
                    }
                }
                AssignmentStatus.submitted -> {
                    Button(onClick = { showSubmitDialog = true }, enabled = true) {
                        Text("Resubmit")
                    }
                }
                AssignmentStatus.graded -> {
                    OutlinedButton(onClick = { /* mock: request review */ }, enabled = false) {
                        Text("Resubmit (disabled)")
                    }
                }
            }
            if (submitNote != null) {
                AssistChip(onClick = {}, label = { Text("Attached note") })
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // Submit/Resubmit dialog (mock)
    if (showSubmitDialog) {
        SubmitDialog(
            initialNote = submitNote.orEmpty(),
            onDismiss = { showSubmitDialog = false },
            onConfirm = { note ->
                submitNote = note.ifBlank { null }
                assignment = assignment.copy(status = AssignmentStatus.submitted)
                showSubmitDialog = false
            }
        )
    }
}

// ----------------- UI pieces & helpers -----------------

@Composable
private fun MissingAssignmentView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Assignment not found")
    }
}

/**
 * Simple dialog to attach a note (mocking file upload).
 */
@Composable
private fun SubmitDialog(
    initialNote: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialNote) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Assignment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Attach a note (optional). File upload is mocked.")
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Note to teacher") },
                    singleLine = false,
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("Submit") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Ticks current Instant every second for live countdown.
 */
@Composable
private fun tickingNow(): State<Instant> {
    val tz = remember { TimeZone.currentSystemDefault() }
    val state = remember { mutableStateOf(Clock.System.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            state.value = Clock.System.now().toLocalDateTime(tz).toInstant(tz)
            delay(1000)
        }
    }
    return state
}

/**
 * Formats a `Duration` into a friendly string:
 *  - "2d 05h 19m 07s" or "00h 03m 09s" when under a day
 *  - For overdue, pass the negative duration as-is (we take abs for text)
 */
private fun formatDuration(duration: kotlin.time.Duration): String {
    val d = duration.absoluteValue
    val totalSeconds = d.inWholeSeconds
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return buildString {
        if (days > 0) append("${days}d ")
        append(String.format("%02dh %02dm %02ds", hours, minutes, seconds))
    }
}
