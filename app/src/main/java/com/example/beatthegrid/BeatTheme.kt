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
val BeatHudPanel = Color(0xCC0B122B)
val BeatHudBorder = Color(0x33FFFFFF)
val BeatGridTrayTop = Color(0xFF1A2344)
val BeatGridTrayBottom = Color(0xFF0E162E)
val BeatGridTrayBorder = Color(0xFF2C3B66)
val BeatOutline = Color(0xFF2E3A5F)
val BeatTarget = Color(0xFFF7B548)
val BeatTargetDeep = Color(0xFFF59E0B)
val BeatGreen = Color(0xFF2EEB87)
val BeatGreenDeep = Color(0xFF1DCB6E)
val BeatGreenGlow = Color(0xFF7CFFB8)
val BeatRed = Color(0xFFFB7185)
val BeatRedDeep = Color(0xFFF43F5E)
val BeatBlue = Color(0xFF60A5FA)
val BeatBlueDeep = Color(0xFF3B82F6)
val BeatTileBase = Color(0xFF1F2A55)
val BeatTileHighlight = Color(0xFF3D5A9A)
val BeatTileShadow = Color(0xFF162245)
val BeatTileUsed = Color(0xFF36435B)
val BeatTileUsedDark = Color(0xFF232B3F)
val BeatTileEdgeHighlight = Color(0xFF6B89CC)
val BeatTileEdgeShadow = Color(0xFF11172F)
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
        fontSize = 40.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp
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
