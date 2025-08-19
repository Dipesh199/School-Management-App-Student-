package com.anever.school.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = AmberSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    tertiary = EmeraldTertiary,
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    background = SurfaceSoft,
    surface = SurfaceSoft,
    error = androidx.compose.ui.graphics.Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = BluePrimary,
    secondary = AmberSecondary,
    tertiary = EmeraldTertiary,
    background = SurfaceSoftDark,
    surface = SurfaceSoftDark
)

private val SchoolShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val SchoolTypography = Typography(
    // Lean punchier headers; keep system sans for simplicity
    titleLarge = Typography().titleLarge.copy(fontFamily = FontFamily.SansSerif),
    titleMedium = Typography().titleMedium.copy(fontFamily = FontFamily.SansSerif),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = FontFamily.SansSerif),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = FontFamily.SansSerif),
    labelMedium = Typography().labelMedium.copy(fontFamily = FontFamily.SansSerif)
)

@Composable
fun SchoolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = SchoolTypography,
        shapes = SchoolShapes,
        content = content
    )
}
