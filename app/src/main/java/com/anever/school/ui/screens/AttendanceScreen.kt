package com.anever.school.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTab(repo: Repository) {
    val tz = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.now().toLocalDateTime(tz).date }

    var yearMonth by remember {
        mutableStateOf(today.year to today.monthNumber)
    }

    val (year, month) = yearMonth
    val marks = remember(yearMonth) { repo.getMonthMarks(year, month) }
    val dayOffset = firstDayOffset(year, month) // Monday=0 .. Sunday=6

    // bottom sheet state
    var sheetDate by remember { mutableStateOf<LocalDate?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    // date range picker state (for Leave dialog)
    var showLeave by remember { mutableStateOf(false) }

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
                    DayCell(
                        date = cell.date,
                        status = cell.status,
                        isToday = cell.date == today,
                        onClick = {
                            sheetDate = cell.date
                            showSheet = true
                        }
                    )
                }
            }
        }

        // Legend
        LegendRow()

        // Trend (unchanged)
        val trend = remember { repo.get30DayTrend() }
        ElevatedCard(Modifier.padding(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("30-day Attendance Trend", fontWeight = FontWeight.SemiBold)
                Sparkline(values = trend)
                Text("${trend.lastOrNull() ?: 0}% present yesterday", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Request Leave
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { showLeave = true }) { Text("Request Leave") }
        }

        // Bottom sheet with per-day details
        if (showSheet && sheetDate != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false }
            ) {
                val details = remember(sheetDate) { repo.getDaySubjectMarks(sheetDate!!) }
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Attendance on ${sheetDate!!}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (details.isEmpty()) {
                        Text("No records for this day.")
                    } else {
                        details.forEach { row ->
                            ListItem(
                                headlineContent = { Text(row.subject.name, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(row.subject.code) },
                                trailingContent = {
                                    StatusChip(row.status)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // Leave dialog with Date Range Picker
        if (showLeave) {
            LeaveDialogWithRange(
                onDismiss = { showLeave = false },
                onSubmit = { reason, from, to ->
                    repo.submitLeaveRequest(reason, from, to)
                    showLeave = false
                }
            )
        }
    }
}

@Composable
private fun LegendRow() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = MaterialTheme.colorScheme.primary, label = "Present")
        LegendItem(color = MaterialTheme.colorScheme.tertiary, label = "Late")
        LegendItem(color = MaterialTheme.colorScheme.error, label = "Absent")
        Spacer(Modifier.weight(1f))
        AssistChip(onClick = {}, label = { Text("Today") },
            leadingIcon = {
                Box(Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
            }
        )
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Dot(color)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun DayCell(date: LocalDate, status: AttendanceStatus?, isToday: Boolean, onClick: () -> Unit) {
    val borderColor = when {
        isToday -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val dot: (@Composable () -> Unit)? = when (status) {
        AttendanceStatus.Present -> { { Dot(MaterialTheme.colorScheme.primary) } }
        AttendanceStatus.Absent -> { { Dot(MaterialTheme.colorScheme.error) } }
        AttendanceStatus.Late -> { { Dot(MaterialTheme.colorScheme.tertiary) } }
        null -> null
    }
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .border(width = if (isToday) 2.dp else 1.dp, color = borderColor, shape = MaterialTheme.shapes.small)
            .clickable { onClick() }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal)
        Spacer(Modifier.height(2.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (dot != null) dot() else Box(Modifier.size(8.dp))
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun StatusChip(status: AttendanceStatus?) {
    val (label, colors) = when (status) {
        AttendanceStatus.Present -> "Present" to AssistChipDefaults.assistChipColors()
        AttendanceStatus.Late -> "Late" to AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        AttendanceStatus.Absent -> "Absent" to AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            labelColor = MaterialTheme.colorScheme.onErrorContainer
        )
        null -> "—" to AssistChipDefaults.assistChipColors()
    }
    AssistChip(onClick = {}, label = { Text(label) }, colors = colors)
}

@Composable
private fun Dot(color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier.size(10.dp).clip(CircleShape).background(color)
    )
}

/* ---------------- Subject Tab (unchanged core) ---------------- */

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

/* ---------------- Leave Dialog with Date Range Picker ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaveDialogWithRange(
    onDismiss: () -> Unit,
    onSubmit: (reason: String, from: LocalDate, to: LocalDate) -> Unit
) {
    val tz = remember { TimeZone.currentSystemDefault() }
    var reason by remember { mutableStateOf("") }
    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    if (showPicker) {
        val state = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = state.selectedStartDateMillis
                    val end = state.selectedEndDateMillis
                    if (start != null && end != null) {
                        fromDate = millisToLocalDate(start, tz)
                        toDate = millisToLocalDate(end, tz)
                        showPicker = false
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        ) {
            DateRangePicker(state = state, title = { Text("Select leave dates") })
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Leave") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = fromDate?.toString() ?: "",
                        onValueChange = {},
                        label = { Text("From") },
                        modifier = Modifier.weight(1f),
                        enabled = false
                    )
                    OutlinedTextField(
                        value = toDate?.toString() ?: "",
                        onValueChange = {},
                        label = { Text("To") },
                        modifier = Modifier.weight(1f),
                        enabled = false
                    )
                }
                TextButton(onClick = { showPicker = true }) { Text("Pick date range") }
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    reason.isBlank() -> error = "Reason is required."
                    fromDate == null || toDate == null -> error = "Please choose a date range."
                    toDate!! < fromDate!! -> error = "To date must be after From date."
                    else -> {
                        onSubmit(reason.trim(), fromDate!!, toDate!!)
                    }
                }
            }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/* ---------------- Utils ---------------- */

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
    val min = values.min()
    val max = values.max()
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
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
        drawLine(color = outlineVariantColor, start = Offset(0f, h - 1f), end = Offset(w, h - 1f), strokeWidth = 1f, alpha = 0.5f)
    }
}

private fun millisToLocalDate(millis: Long, tz: TimeZone): LocalDate {
    val instant = Instant.fromEpochMilliseconds(millis)
    return instant.toLocalDateTime(tz).date
}
