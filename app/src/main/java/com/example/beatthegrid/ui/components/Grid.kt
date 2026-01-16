package com.example.beatthegrid.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.ripple.rememberRipple
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TileState {
    Available,
    Used,
    Selected
}

internal fun LazyGridScope.gridTiles(
    grid: List<Int>,
    usedIndices: Set<Int>,
    selectedIndex: Int?,
    enabled: Boolean = true,
    onCellSelected: (Int) -> Unit
) {
    itemsIndexed(grid) { index, value ->
        val used = usedIndices.contains(index)
        val selected = selectedIndex == index
        val tileState = when {
            selected -> TileState.Selected
            used -> TileState.Used
            else -> TileState.Available
        }
        GridTile(
            value = value,
            state = tileState,
            enabled = enabled && !used,
            onClick = { onCellSelected(index) }
        )
    }
}

@Composable
fun GridTile(
    value: Int,
    state: TileState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val selectedBackground = colorScheme.errorContainer
    val selectedBorder = colorScheme.error
    val backgroundColor = when (state) {
        TileState.Available -> colorScheme.surfaceVariant
        TileState.Selected -> selectedBackground
        TileState.Used -> colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val borderColor = when (state) {
        TileState.Available -> colorScheme.outlineVariant
        TileState.Selected -> selectedBorder
        TileState.Used -> colorScheme.outlineVariant.copy(alpha = 0.4f)
    }
    val contentColor = when (state) {
        TileState.Available -> colorScheme.onSurface
        TileState.Selected -> Color.White
        TileState.Used -> colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val ripple = rememberRipple()

    Surface(
        modifier = Modifier
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = ripple
            )
            .aspectRatio(1f)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        tonalElevation = if (state == TileState.Selected) 2.dp else 0.dp,
        border = BorderStroke(if (state == TileState.Selected) 2.dp else 1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            if (state == TileState.Used) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Used",
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )
            }
        }
    }
}
