package com.example.beatthegrid

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    secondary = Color(0xFF7DD3FC),
    tertiary = Color(0xFFF59E0B),
    background = Color(0xFF0E1116),
    surface = Color(0xFF151A21),
    surfaceVariant = Color(0xFF1F2630),
    outline = Color(0xFF3C4656),
    outlineVariant = Color(0xFF2B3341),
    error = Color(0xFFFCA5A5),
    onPrimary = Color(0xFF0B1220),
    onSecondary = Color(0xFF0B1220),
    onTertiary = Color(0xFF2B1700),
    onBackground = Color(0xFFE6E9EF),
    onSurface = Color(0xFFE6E9EF),
    onSurfaceVariant = Color(0xFFB6C0CF),
    onError = Color(0xFF2B0A0A)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1D4ED8),
    secondary = Color(0xFF0EA5E9),
    tertiary = Color(0xFFB45309),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEFF3F8),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFB91C1C),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    onError = Color(0xFFFFFFFF)
)

private val BeatTypography = Typography(
    displaySmall = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.2.sp
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.3.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp
    )
)

@Composable
fun BeatTheGridTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = BeatTypography,
        content = content
    )
}
