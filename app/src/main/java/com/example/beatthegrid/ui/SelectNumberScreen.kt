package com.example.beatthegrid.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.beatthegrid.AttemptOutcome
import com.example.beatthegrid.GameState
import com.example.beatthegrid.ui.components.ProgressCard
import com.example.beatthegrid.ui.components.gridTiles

@Composable
fun SelectNumberScreen(
    state: GameState,
    onCellSelected: (Int) -> Unit,
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    onBack: () -> Unit
) {
    AppScaffold(
        title = "Beat the Grid",
        subtitle = "Pick a cell",
        onBack = onBack
    ) {
        ProgressCard(state = state)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                gridTiles(
                    grid = state.grid,
                    usedIndices = state.usedIndices,
                    selectedIndex = state.selectedIndex,
                    onCellSelected = onCellSelected
                )
            }
        }
        if (state.result == AttemptOutcome.Won) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "Nice!") },
                text = { Text(text = "You beat this grid. Ready for the next one?") },
                confirmButton = {
                    TextButton(onClick = onNextLevel) {
                        Text(text = "Next grid")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onHome) {
                        Text(text = "Home")
                    }
                }
            )
        } else if (state.result == AttemptOutcome.Lost) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "Try again") },
                text = { Text(text = "You ran out of moves.") },
                confirmButton = {
                    TextButton(
                        enabled = state.triesRemaining > 0,
                        onClick = onRetry
                    ) {
                        Text(text = "Retry same grid")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onHome) {
                        Text(text = "Home")
                    }
                }
            )
        }
    }
}
