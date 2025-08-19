package com.anever.school.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeaders(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
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

/** Solid color card with thin top bar. */
@Composable
fun GradientCard( // name kept for compatibility; now solid color
    modifier: Modifier = Modifier,
    topBarHeight: Int = 6,
    topBarColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(modifier) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(topBarHeight.dp)
                    .background(topBarColor)
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

/** Solid hero header (no gradients). */
@Composable
fun HeroHeader(
    title: String,
    subtitle: String? = null,
    bgColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(tonalElevation = 0.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

//@Composable
//fun TopBarLarge(
//    title: String,
//    onBell: (() -> Unit)? = null,
//    onAvatar: (() -> Unit)? = null,
//    trailing: (@Composable RowScope.() -> Unit)? = null
//) {
//    Surface(shadowElevation = 2.dp, tonalElevation = 0.dp) {
//        Row(
//            Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
//            Spacer(Modifier.weight(1f))
//            if (trailing != null) trailing() else {
//                IconButton(onClick = { onBell?.invoke() }) {
//                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
//                }
//                IconButton(onClick = { onAvatar?.invoke() }) {
//                    Icon(Icons.Outlined.Person, contentDescription = "Profile")
//                }
//            }
//        }
//    }
//}

@Composable
fun Tile(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    minHeight: Dp = 96.dp,
    accent: Color = MaterialTheme.colorScheme.primary
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
                    .background(accent.copy(alpha = 0.12f)),
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
                .background(accent)
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

/** Round action (solid color). */
@Composable
fun RoundAction(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    size: Dp = 84.dp,
    bgColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = size)) {
        ElevatedCard(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(size),
            colors = CardDefaults.elevatedCardColors(containerColor = bgColor)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = Color.White)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.labelMedium)
    }
}
