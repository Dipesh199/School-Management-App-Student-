package com.anever.school.ui.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/* ---------- Solid color palette (stable mapping) ---------- */
private val Palette = listOf(
    Color(0xFF3B82F6), // Blue
    Color(0xFFF59E0B), // Amber
    Color(0xFF10B981), // Emerald
    Color(0xFFA78BFA), // Violet
    Color(0xFFEC4899), // Pink
    Color(0xFFF43F5E), // Rose
    Color(0xFF22D3EE), // Cyan
    Color(0xFF84CC16), // Lime
    Color(0xFFFB923C), // Orange
    Color(0xFF6366F1)  // Indigo
)

fun eduColor(seed: String): Color {
    var h = 0
    for (c in seed) h = (h * 31 + c.code) and 0x7fffffff
    return Palette[h % Palette.size]
}

/* Helpers to get “pastel” blocks that read well on light/dark */
private fun lighten(c: Color, t: Float) = lerp(c, Color.White, t.coerceIn(0f, 1f))
private fun darken(c: Color, t: Float) = lerp(c, Color.Black, t.coerceIn(0f, 1f))
private fun pastel(seed: String): Pair<Color, Color> { // bg, fg
    val base = eduColor(seed)
    val bg = lighten(base, 0.82f)
    val fg = darken(base, 0.20f)
    return bg to fg
}

/* ---------- App bar & section headers ---------- */
@Composable
fun TopBarLarge(
    title: String,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onBell: (() -> Unit)? = null,
    onAvatar: (() -> Unit)? = null
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

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/* ---------- Solid hero header ---------- */
@Composable
fun EduHeroHeader(
    title: String,
    subtitle: String? = null,
    seed: String = title,
    trailing: (@Composable ColumnScope.() -> Unit)? = null
) {
    val bg = eduColor(seed)
    Surface {
        Row(
            Modifier
                .fillMaxWidth()
                .background(bg)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
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
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }
            if (trailing != null) Column(horizontalAlignment = Alignment.CenterHorizontally, content = trailing)
        }
    }
}

@Composable
fun EduHeroHeaderMainScreen(
    title: String,
    subtitle: String? = null,
    seed: String = title,
    trailing: (@Composable ColumnScope.() -> Unit)? = null
) {
    val bg = eduColor(seed)
    Surface {
        Row(
            Modifier
                .fillMaxWidth()
                .background(bg)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
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
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }
            if (trailing != null) Column(horizontalAlignment = Alignment.CenterHorizontally, content = trailing)
        }
    }
}

/* ---------- Cards, chips, round actions (solid colors) ---------- */
@Composable
fun EduCard(
    modifier: Modifier = Modifier,
    seed: String = "card",
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(eduColor(seed))
            )
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun StatusChip(text: String, color: Color) {
    AssistChip(
        onClick = {},
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.14f),
            labelColor = color
        )
    )
}

/* Round action (kept for places that still use it) */
@Composable
fun RoundAction(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    seed: String = title,
    size: Dp = 84.dp
) {
    val bg = eduColor(seed)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = size)) {
        ElevatedCard(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(size),
            colors = CardDefaults.elevatedCardColors(containerColor = bg)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = Color.White)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.labelMedium)
    }
}

/* ---------- NEW: PastelTile & CategoryBubble (match screenshot) ---------- */
@Composable
fun PastelTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    seed: String = title,
    minHeight: Dp = 110.dp
) {
    val (bg, fg) = pastel(seed)
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(fg.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = fg)
            }
            Text(title, style = MaterialTheme.typography.labelLarge, color = fg)
        }
    }
}

@Composable
fun CategoryBubble(
    title: String,
    icon: ImageVector,
    seed: String = title,
    onClick: () -> Unit = {}
) {
    val (bg, fg) = pastel(seed)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ElevatedCard(
            onClick = onClick,
            shape = CircleShape,
            colors = CardDefaults.elevatedCardColors(containerColor = bg)
        ) {
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = fg)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(title, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

/* ---------- Progress ring (chart) — gradient ALLOWED here ---------- */
@Composable
fun ProgressRing(
    progress: Float, // 0f..1f
    size: Dp = 64.dp,
    thickness: Dp = 8.dp,
    seed: String = "progress"
) {
    val c1 = eduColor(seed)
    val c2 = eduColor(seed + "_alt")
    val ringBrush = Brush.linearGradient(listOf(c1, c2))

    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val stroke = Stroke(width = thickness.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = Color.Black.copy(alpha = 0.08f),
                startAngle = 135f, sweepAngle = 270f,
                useCenter = false, style = stroke
            )
            drawArc(
                brush = ringBrush,
                startAngle = 135f, sweepAngle = 270f * progress.coerceIn(0f, 1f),
                useCenter = false, style = stroke
            )
        }
        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}
