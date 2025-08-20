package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
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
    val cats = listOf(
        HomeCat("Biology", Icons.Filled.Science),
        HomeCat("Animals", Icons.Filled.Pets),
        HomeCat("Geography", Icons.Filled.Public),
        HomeCat("Science", Icons.Filled.Science)
    )

    val actions = listOf(
        QuickAction("Exams", Icons.Filled.School, onOpenExamSchedule),
        QuickAction("Notices", Icons.Filled.Campaign, onOpenNotices),
        QuickAction("Events", Icons.Filled.ConfirmationNumber, onOpenEvents),
        QuickAction("Lost & Found", Icons.Filled.Search, onOpenLostFound),
        QuickAction("Attendance", Icons.Filled.EventAvailable, onOpenAttendance),
        QuickAction("Transport", Icons.Filled.DirectionsBus, onOpenTransport),
        QuickAction("Library", Icons.Filled.LocalLibrary, onOpenLibrary),
    )

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(title = "Featured AR Gallery", subtitle = "Explore and learn something new", seed = "home") {
            Spacer(Modifier.height(4.dp))
            ProgressRing(progress = 0.82f, seed = "attendance")
            Spacer(Modifier.height(4.dp))
            Text("Attendance", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White)
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cats) { c -> CategoryBubble(title = c.title, icon = c.icon, seed = c.title) }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(actions) { it -> PastelTile(title = it.title, icon = it.icon, onClick = it.onClick, seed = it.title) }
        }
    }
}

private data class QuickAction(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

private data class HomeCat(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
