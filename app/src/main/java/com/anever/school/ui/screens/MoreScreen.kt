package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anever.school.ui.design.Tile
import com.anever.school.ui.design.TopBarLarge


@Composable
fun MoreScreen(
    onOpenAttendance: () -> Unit,
    onOpenNotices: () -> Unit,
    onOpenTransport: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenLostFound: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopBarLarge(title = "More")
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                listOf(
                    Triple("My Attendance", "Calendar, stats, leave", onOpenAttendance) to Icons.Filled.EventAvailable,
                    Triple("Notices", "School & class updates", onOpenNotices) to Icons.Filled.Campaign,
                    Triple("Transport", "Route, stop & ETA", onOpenTransport) to Icons.Filled.DirectionsBus,
                    Triple("Library", "Browse & loans", onOpenLibrary) to Icons.Filled.LocalLibrary,
                    Triple("Event Passes", "Fests & seminars", onOpenEvents) to Icons.Filled.ConfirmationNumber,
                    Triple("Lost & Found", "Report & alerts", onOpenLostFound) to Icons.Filled.Search
                )
            ) { (item, icon) ->
                val (title, sub, click) = item
                Tile(
                    title = title,
                    subtitle = sub,
                    icon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = click
                )
            }
        }
    }
}