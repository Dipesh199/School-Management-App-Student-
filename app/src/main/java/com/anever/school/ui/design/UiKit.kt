package com.anever.school.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    topBarHeight: Int = 6,
    gradient: Brush = Brush.horizontalGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(modifier) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(topBarHeight.dp)
                    .background(gradient)
            )
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun StatPill(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

/** Big friendly header with gradient background; drop it at top of screens. */
@Composable
fun HeroHeader(
    title: String,
    subtitle: String? = null,
    gradient: Brush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary
        )
    )
) {
    Surface(tonalElevation = 0.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun TopBarLarge(
    title: String,
    onBell: (() -> Unit)? = null,
    onAvatar: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Surface(shadowElevation = 2.dp, tonalElevation = 0.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            if (trailing != null) trailing() else {
                IconButton(onClick = { onBell?.invoke() }) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                }
                IconButton(onClick = { onAvatar?.invoke() }) {
                    Icon(Icons.Outlined.Person, contentDescription = "Profile")
                }
            }
        }
    }
}

/** Generic list tile (kept for other screens). */
@Composable
fun Tile(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    minHeight: Dp = 96.dp
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { icon() }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
        )
    }
}

@Composable
fun EmptyIllustration(text: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("(•‿•)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Circular action button for the Home quick actions grid. Now accepts a custom background brush. */
@Composable
fun RoundAction(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    size: Dp = 84.dp,
    backgroundBrush: Brush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        )
    )
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = size)) {
        ElevatedCard(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(size)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(backgroundBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.labelMedium)
    }
}
