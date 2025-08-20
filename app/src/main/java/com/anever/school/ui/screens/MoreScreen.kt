package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anever.school.ui.design.EduHeroHeader
import com.anever.school.ui.design.PastelTile

@Composable
fun MoreScreen(
    onOpenAttendance: () -> Unit,
    onOpenNotices: () -> Unit,
    onOpenTransport: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenLostFound: () -> Unit
) {
    val actions = listOf(
        QuickLink("Attendance", Icons.Filled.EventAvailable, onOpenAttendance),
        QuickLink("Notices", Icons.Filled.Campaign, onOpenNotices),
        QuickLink("Transport", Icons.Filled.DirectionsBus, onOpenTransport),
        QuickLink("Library", Icons.Filled.LocalLibrary, onOpenLibrary),
        QuickLink("Events", Icons.Filled.ConfirmationNumber, onOpenEvents),
        QuickLink("Lost & Found", Icons.Filled.Search, onOpenLostFound),
    )

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(title = "More", subtitle = "Quick tools & sections", seed = "more")

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(actions) { it ->
                PastelTile(title = it.title, icon = it.icon, onClick = it.onClick, seed = it.title)
            }
        }
    }
}

private data class QuickLink(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)
