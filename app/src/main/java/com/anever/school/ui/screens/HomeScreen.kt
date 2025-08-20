package com.anever.school.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anever.school.R
import com.anever.school.ui.design.EduHeroHeader
import com.anever.school.ui.design.PastelTile
import com.anever.school.ui.design.eduColor

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
        QuickAction("Exams", Icons.Filled.School, onOpenExamSchedule),
        QuickAction("Notices", Icons.Filled.Campaign, onOpenNotices),
        QuickAction("Events", Icons.Filled.ConfirmationNumber, onOpenEvents),
        QuickAction("Lost & Found", Icons.Filled.Search, onOpenLostFound),
        QuickAction("Attendance", Icons.Filled.EventAvailable, onOpenAttendance),
        QuickAction("Transport", Icons.Filled.DirectionsBus, onOpenTransport),
        QuickAction("Library", Icons.Filled.LocalLibrary, onOpenLibrary),
    )

    Column(Modifier.fillMaxSize()) {
        EduHeroHeader(
            title = "Home",
            subtitle = "Explore and learn something new",
            seed = "home"
        ) {
            // Only the app logo in header (attendance removed)
            LogoTile()
        }

        // Pastel action tiles
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

/* ------------ Helpers ------------ */

private data class QuickAction(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

private data class HomeCat(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/** Square, rounded tile with the app icon (replace drawable if you have a custom logo). */
@Composable
private fun LogoTile() {
    val seed = "AppLogo"
    val base = eduColor(seed)
    val bg = lerp(base, Color.White, 0.82f)
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = bg),
        modifier = Modifier.size(72.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.zen_logo_square_transparent),
                contentDescription = "App logo",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
