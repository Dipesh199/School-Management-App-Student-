package com.anever.school.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MoreScreen() {
    Column(Modifier.padding(16.dp)) {
        Text("More")
        Text("Notices, Profile, Settings (placeholders)")
    }
}
