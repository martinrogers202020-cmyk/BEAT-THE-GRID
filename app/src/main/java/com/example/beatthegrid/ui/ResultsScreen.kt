package com.example.beatthegrid.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.beatthegrid.AttemptOutcome
import com.example.beatthegrid.GameState
import com.example.beatthegrid.ui.components.PrimaryCard
import com.example.beatthegrid.ui.components.gridTiles
import java.util.Locale

@Composable
fun ResultsScreen(
    state: GameState,
    onShare: (String) -> Unit,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val resultText = if (state.result == AttemptOutcome.Won) "Victory" else "Out of moves"
    val shareMessage = buildShareText(state)

    AppScaffold(
        title = "Beat the Grid",
        subtitle = "Results",
        onBack = onHome,
        actions = {
            IconButton(onClick = { onShare(shareMessage) }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        }
    ) {
        PrimaryCard(title = resultText) {
            Text(
                text = "Target",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = state.target.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Final value: ${state.runningValue}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Moves used: ${state.moveIndex} / 4",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "Used grid",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                gridTiles(
                    grid = state.grid,
                    usedIndices = state.usedIndices,
                    selectedIndex = null,
                    enabled = false,
                    onCellSelected = {}
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                enabled = state.triesRemaining > 0 && state.result == AttemptOutcome.Lost,
                onClick = onPlayAgain
            ) {
                Text(text = "Try again")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onHome
            ) {
                Text(text = "Daily")
            }
        }
    }
}

fun buildShareText(state: GameState): String {
    val status = if (state.result == AttemptOutcome.Won) "WIN" else "LOSE"
    val dayLabel = String.format(Locale.US, "%d", state.dayIndex + 1)
    return "Beat the Grid Day $dayLabel: $status. Target ${state.target}, final ${state.runningValue}. Moves ${state.moveIndex}/4."
}
