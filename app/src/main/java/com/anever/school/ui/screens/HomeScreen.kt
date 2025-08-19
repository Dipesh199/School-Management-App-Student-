package com.anever.school.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anever.school.ui.design.HeroHeader
import com.anever.school.ui.design.RoundAction
import com.anever.school.ui.design.TopBarLarge

@Composable
fun HomeScreen(
    onOpenExamSchedule: () -> Unit,
    onOpenNotices: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenLostFound: () -> Unit,
    onOpenAttendance: () -> Unit = {},
    onOpenTransport: () -> Unit = {},
    onOpenLibrary: () -> Unit = {}
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

    // Bright palette; mapping by index gives a colorful, stable layout.
    val palette = listOf(
        Color(0xFF60A5FA), // Sky
        Color(0xFFF59E0B), // Amber
        Color(0xFF34D399), // Mint
        Color(0xFFA78BFA), // Grape
        Color(0xFFEC4899), // Pink
        Color(0xFFF43F5E), // Rose
        Color(0xFF22D3EE), // Cyan
        Color(0xFF84CC16), // Lime
        Color(0xFFFB923C), // Orange
        Color(0xFF6366F1)  // Indigo
    )

    fun colorfulBrush(index: Int): Brush {
        val c1 = palette[index % palette.size]
        val c2 = palette[(index + 3) % palette.size] // offset to avoid same-color blends
        return Brush.linearGradient(listOf(c1.copy(alpha = 0.85f), c2.copy(alpha = 0.85f)))
    }

    Column(Modifier.fillMaxSize()) {
        TopBarLarge(title = "Home")
        HeroHeader(title = "Welcome 👋", subtitle = "Quick actions at a glance")

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(actions) { idx, it ->
                RoundAction(
                    title = it.title,
                    icon = it.icon,
                    onClick = it.onClick,
                    backgroundBrush = colorfulBrush(idx)
                )
            }
        }
    }
}

private data class HomeQuick(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)
