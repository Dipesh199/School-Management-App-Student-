package com.anever.school.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem

@Composable
fun MoreScreen(
    onOpenAttendance: () -> Unit,
    onOpenNotices: () -> Unit
) {
    Column(Modifier.padding(16.dp)) {
        Text("More")
        ListItem(
            headlineContent = { Text("My Attendance") },
            supportingContent = { Text("Calendar, subject stats, leave request") },
            modifier = Modifier.clickable { onOpenAttendance() }
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Notices / Announcements") },
            supportingContent = { Text("School & class notices, exams, events") },
            modifier = Modifier.clickable { onOpenNotices() }
        )
    }
}
