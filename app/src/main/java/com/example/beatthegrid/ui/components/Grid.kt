package com.example.beatthegrid.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val backgroundColor = when (state) {
        TileState.Available -> colorScheme.surfaceVariant
        TileState.Selected -> colorScheme.primaryContainer
        TileState.Used -> colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val borderColor = when (state) {
        TileState.Available -> colorScheme.outlineVariant
        TileState.Selected -> colorScheme.primary
        TileState.Used -> colorScheme.outlineVariant.copy(alpha = 0.4f)
    }
    val contentColor = when (state) {
        TileState.Available -> colorScheme.onSurface
        TileState.Selected -> colorScheme.onPrimaryContainer
        TileState.Used -> colorScheme.onSurfaceVariant
    }
    val tileModifier = if (enabled) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = tileModifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        tonalElevation = if (state == TileState.Selected) 2.dp else 0.dp,
        border = BorderStroke(1.dp, borderColor)
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
        }
    }
}
