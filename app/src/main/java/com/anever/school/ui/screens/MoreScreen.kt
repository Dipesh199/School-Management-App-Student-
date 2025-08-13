package com.anever.school.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ListItem

@Composable
fun MoreScreen(onOpenAttendance: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text("More")
        ElevatedCard(Modifier.padding(top = 12.dp)) {
            ListItem(
                headlineContent = { Text("My Attendance") },
                supportingContent = { Text("Calendar, subject stats, leave request") },
                modifier = Modifier.padding(horizontal = 8.dp).clickable{ onOpenAttendance()},
                overlineContent = {},
                trailingContent = {},
                leadingContent = null
            )
        }
    }
}

