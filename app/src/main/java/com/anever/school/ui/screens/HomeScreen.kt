package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anever.school.ui.design.*

@Composable
fun HomeScreen(
    onOpenExamSchedule: () -> Unit,
    onOpenNotices: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenLostFound: () -> Unit,
    onOpenAttendance: () -> Unit = {},
    onOpenTransport: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
) {
    val actions = listOf(
        HomeQuick("Exams", Icons.Filled.School, onOpenExamSchedule),
        HomeQuick("Notices", Icons.Filled.Campaign, onOpenNotices),
        HomeQuick("Events", Icons.Filled.ConfirmationNumber, onOpenEvents),
        HomeQuick("Lost & Found", Icons.Filled.Search, onOpenLostFound),
        HomeQuick("Attendance", Icons.Filled.EventAvailable, onOpenAttendance),
        HomeQuick("Transport", Icons.Filled.DirectionsBus, onOpenTransport),
        HomeQuick("Library", Icons.Filled.LocalLibrary, onOpenLibrary),
    )

    Column(Modifier.fillMaxSize()) {
//        TopBarLarge(title = "Home")
        EduHeroHeader(
            title = "Zen School",
            subtitle = "Welcome back! Keep learning ✨",
            seed = "home"
        ) {
            Spacer(Modifier.height(4.dp))
            ProgressRing(progress = 0.82f, seed = "attendance")
            Spacer(Modifier.height(4.dp))
            Text("Attendance", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(actions) { it ->
                RoundAction(title = it.title, icon = it.icon, onClick = it.onClick, seed = it.title)
            }
        }
    }
}

private data class HomeQuick(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)
