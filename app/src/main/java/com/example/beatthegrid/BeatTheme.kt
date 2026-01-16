package com.example.beatthegrid

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val BeatBackgroundTop = Color(0xFF0A0F2B)
val BeatBackgroundMid = Color(0xFF131C3D)
val BeatBackgroundBottom = Color(0xFF070B1C)
val BeatCard = Color(0xFF1A2445)
val BeatCardSecondary = Color(0xFF111A33)
val BeatOutline = Color(0xFF2E3A5F)
val BeatTarget = Color(0xFFF7B548)
val BeatTargetDeep = Color(0xFFF59E0B)
val BeatGreen = Color(0xFF2EEB87)
val BeatGreenDeep = Color(0xFF1DCB6E)
val BeatRed = Color(0xFFFB7185)
val BeatRedDeep = Color(0xFFF43F5E)
val BeatBlue = Color(0xFF60A5FA)
val BeatBlueDeep = Color(0xFF3B82F6)
val BeatTileBase = Color(0xFF24325C)
val BeatTileHighlight = Color(0xFF334070)
val BeatTileUsed = Color(0xFF3B4259)
val BeatTileUsedDark = Color(0xFF2A3146)
val BeatTileEdgeHighlight = Color(0xFF4A5B8E)
val BeatTileEdgeShadow = Color(0xFF1A223F)
val BeatDisabledButton = Color(0xFF4B5563)
val BeatDisabledButtonDeep = Color(0xFF374151)
val BeatOnDark = Color(0xFFE5E7EB)
val BeatMuted = Color(0xFF9CA3AF)

private val BeatColorScheme = darkColorScheme(
    primary = BeatGreen,
    secondary = BeatBlue,
    tertiary = BeatTarget,
    background = BeatBackgroundBottom,
    surface = BeatCard,
    onSurface = BeatOnDark,
    onBackground = BeatOnDark
)

private val BeatTypography = Typography(
    displayMedium = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 2.sp
    ),
    displaySmall = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.5.sp
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
)

@Composable
fun BeatTheGridTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BeatColorScheme,
        typography = BeatTypography,
        content = content
    )
}

fun beatBackgroundGradient(): Brush {
    return Brush.verticalGradient(
        colors = listOf(BeatBackgroundTop, BeatBackgroundMid, BeatBackgroundBottom)
    )
}
