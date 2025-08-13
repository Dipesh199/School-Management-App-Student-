package com.anever.school.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.data.model.AttendanceStatus
import kotlinx.datetime.*

@Composable
fun AttendanceScreen() {
    val repo = remember { Repository() }
    var tab by remember { mutableIntStateOf(0) } // 0=Calendar, 1=By Subject

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Calendar") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("By Subject") })
        }
        when (tab) {
            0 -> CalendarTab(repo)
            1 -> SubjectTab(repo)
        }
    }
}

/* ---------------- Calendar Tab ---------------- */

@Composable
private fun CalendarTab(repo: Repository) {
    val tz = remember { TimeZone.currentSystemDefault() }
    var yearMonth by remember {
        val today = Clock.System.now().toLocalDateTime(tz).date
        mutableStateOf(today.year to today.monthNumber)
    }

    val (year, month) = yearMonth
    val marks = remember(yearMonth) { repo.getMonthMarks(year, month) }
    val dayOffset = firstDayOffset(year, month) // Monday=0 .. Sunday=6

    Column(Modifier.fillMaxSize()) {
        // Header + actions
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${monthName(month)} $year", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = { yearMonth = prevMonth(year, month) }) { Text("Prev") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = { yearMonth = nextMonth(year, month) }) { Text("Next") }
        }

        // Weekday header
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach {
                Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
            }
        }

        // Grid (7 cols). Add leading blanks = dayOffset
        val cells = List(dayOffset) { null } + marks.map { it }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(cells) { cell ->
                if (cell == null) {
                    Box(Modifier.aspectRatio(1f))
                } else {
                    DayCell(cell.date.dayOfMonth, cell.status)
                }
            }
        }

        // Trend + request
        val trend = remember { repo.get30DayTrend() }
        ElevatedCard(Modifier.padding(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("30-day Attendance Trend", fontWeight = FontWeight.SemiBold)
                Sparkline(values = trend)
                Text("${trend.lastOrNull() ?: 0}% present yesterday", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Request Leave
        var showDialog by remember { mutableStateOf(false) }
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { showDialog = true }) { Text("Request Leave") }
        }
        if (showDialog) {
            LeaveDialog(
                onDismiss = { showDialog = false },
                onSubmit = { reason, from, to ->
                    repo.submitLeaveRequest(reason, from, to)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
private fun DayCell(day: Int, status: AttendanceStatus?) {
    val dot: (@Composable () -> Unit)? = when (status) {
        AttendanceStatus.Present -> { { Dot(MaterialTheme.colorScheme.primary) } }
        AttendanceStatus.Absent -> { { Dot(MaterialTheme.colorScheme.error) } }
        AttendanceStatus.Late -> { { Dot(MaterialTheme.colorScheme.tertiary) } }
        null -> null
    }
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(day.toString(), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(2.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (dot != null) dot() else Box(Modifier.size(8.dp))
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun Dot(color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier.size(10.dp).clip(CircleShape).background(color)
    )
}

/* ---------------- Subject Tab ---------------- */

@Composable
private fun SubjectTab(repo: Repository) {
    val rows = remember { repo.getSubjectAttendanceSummary() }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Attendance by Subject", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        rows.forEach { r ->
            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(r.subject.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        val warn = r.presentPct < 75
                        AssistChip(
                            onClick = {},
                            label = { Text("${r.presentPct}%") },
                            colors = if (warn)
                                AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            else AssistChipDefaults.assistChipColors()
                        )
                    }
                    LinearProgressIndicator(
                        progress = { r.presentPct / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Text("${r.present}/${r.total} sessions present", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (rows.isEmpty()) Text("No attendance data.")
    }
}

/* ---------------- Dialog & utils ---------------- */

@Composable
private fun LeaveDialog(
    onDismiss: () -> Unit,
    onSubmit: (reason: String, from: LocalDate, to: LocalDate) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var fromText by remember { mutableStateOf("") }
    var toText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Leave") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") })
                OutlinedTextField(value = fromText, onValueChange = { fromText = it }, label = { Text("From (YYYY-MM-DD)") })
                OutlinedTextField(value = toText, onValueChange = { toText = it }, label = { Text("To (YYYY-MM-DD)") })
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val from = runCatching { LocalDate.parse(fromText) }.getOrNull()
                val to = runCatching { LocalDate.parse(toText) }.getOrNull()
                when {
                    reason.isBlank() -> error = "Reason is required."
                    from == null || to == null -> error = "Enter valid dates (YYYY-MM-DD)."
                    to < from -> error = "To date must be after From date."
                    else -> {
                        onSubmit(reason.trim(), from, to)
                        onDismiss()
                    }
                }
            }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun monthName(m: Int): String = Month(m).name.lowercase().replaceFirstChar { it.titlecase() }

private fun firstDayOffset(year: Int, month: Int): Int {
    // Monday=0 .. Sunday=6
    val first = LocalDate(year, month, 1)
    val iso = first.dayOfWeek.isoDayNumber // Mon=1..Sun=7
    return iso - 1
}

private fun prevMonth(year: Int, month: Int): Pair<Int, Int> =
    if (month == 1) (year - 1) to 12 else year to (month - 1)

private fun nextMonth(year: Int, month: Int): Pair<Int, Int> =
    if (month == 12) (year + 1) to 1 else year to (month + 1)

@Composable
private fun Sparkline(values: List<Int>, modifier: Modifier = Modifier) {
    if (values.size <= 1) {
        Canvas(modifier = modifier.fillMaxWidth().height(56.dp)) {}
        return
    }
    val min = values.minOrNull()!!
    val max = values.maxOrNull()!!

// Capture colors in @Composable scope
    val primaryColor = MaterialTheme.colorScheme.primary
    val baselineColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier.fillMaxWidth().height(56.dp)) {
        val w = size.width
        val h = size.height
        val stepX = w / (values.size - 1).coerceAtLeast(1)
        fun norm(v: Int): Float = if (max == min) 0.5f else 1f - ((v - min).toFloat() / (max - min).toFloat())
        var prev = Offset(0f, norm(values.first()) * h)
        values.drop(1).forEachIndexed { idx, v ->
            val x = stepX * (idx + 1)
            val y = norm(v) * h
            val curr = Offset(x, y)
            drawLine(color = primaryColor, start = prev, end = curr, strokeWidth = 3f)
            prev = curr
        }
        // baseline
        drawLine(color = baselineColor, start = Offset(0f, h - 1f), end = Offset(w, h - 1f), strokeWidth = 1f, alpha = 0.5f)
    }
}
