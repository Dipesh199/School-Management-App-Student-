package com.anever.school.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository

// For LazyColumn list overload
import androidx.compose.foundation.lazy.items
// For LazyVerticalGrid list overload
import androidx.compose.foundation.lazy.grid.items

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.anever.school.data.DaySchedule
import com.anever.school.data.TodayClass
import com.anever.school.ui.util.ClassStatus
import com.anever.school.ui.util.dayName
import com.anever.school.ui.util.markStatusesForToday
import kotlinx.datetime.*

@Composable
fun ClassesScreen(onOpenClass: (String) -> Unit) {
    val repo = remember { Repository() }

    var tab by remember { mutableIntStateOf(0) } // 0=Today, 1=Week

    val todayDate = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val todayClasses = remember { repo.getTodayClasses(todayDate) }
    val week = remember { repo.getWeekClasses() }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Today") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Week") })
        }
        when (tab) {
            0 -> TodayTab(todayClasses, todayDate, onOpenClass)
            1 -> WeekTab(week, onOpenClass)
        }
    }
}

@Composable
private fun TodayTab(items: List<TodayClass>, date: LocalDate, onOpenClass: (String) -> Unit) {
    val nowTime = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    }
    val withStatus = remember(items, nowTime) { markStatusesForToday(items, nowTime) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Today (${dayName(date.dayOfWeek.isoDayNumber)})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
        }
        if (withStatus.isEmpty()) {
            item { EmptyState("No classes today") }
        } else {
            items(withStatus) { cw ->
                ClassCard(cw.item, cw.status) { onOpenClass(cw.item.id) }
            }
        }
    }
}

@Composable
private fun WeekTab(week: List<DaySchedule>, onOpenClass: (String) -> Unit) {
    // Show a 2-column grid of day cards
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(week) { day ->
            DayCard(day, onOpenClass)
        }
    }
}

@Composable
private fun DayCard(day: DaySchedule, onOpenClass: (String) -> Unit) {
    ElevatedCard {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                dayName(day.dayOfWeek),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (day.classes.isEmpty()) {
                Text("— No classes —", style = MaterialTheme.typography.bodySmall)
            } else {
                day.classes.forEach { c ->
                    ClassMiniRow(c) { onOpenClass(c.id) }
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun ClassMiniRow(c: TodayClass, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(c.subject) },
        supportingContent = { Text("${c.start} - ${c.end} • Room ${c.room}") },
        trailingContent = {
            TextButton(onClick = onClick) { Text("Open") }
        }
    )
}

@Composable
private fun ClassCard(
    c: TodayClass,
    status: ClassStatus,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }   // ← use clickable instead of ElevatedCard(onClick = …)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(c.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${c.start} - ${c.end} • Room ${c.room}", style = MaterialTheme.typography.bodyMedium)
            Text("Teacher: ${c.teacher}", style = MaterialTheme.typography.bodySmall)
            AssistChip(onClick = onClick, label = {
                Text(
                    when (status) {
                        ClassStatus.ONGOING -> "Ongoing"
                        ClassStatus.NEXT -> "Next"
                        ClassStatus.PAST -> "Finished"
                        ClassStatus.UPCOMING -> "Upcoming"
                    }
                )
            })
        }
    }
}


@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AssistChip(onClick = {}, label = { Text(text) })
    }
}
