package com.example.beatthegrid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.beatthegrid.GameState
import com.example.beatthegrid.Operation
import com.example.beatthegrid.ui.components.PrimaryCard
import com.example.beatthegrid.ui.components.ProgressCard

@Composable
fun ApplyOperationScreen(state: GameState, onApply: (Operation) -> Unit, onBack: () -> Unit) {
    val runningValue = state.runningValue ?: 0
    val selectionValue = state.selectedIndex?.let { state.grid[it] } ?: 0

    AppScaffold(
        title = "Beat the Grid",
        subtitle = "Apply operation",
        onBack = onBack
    ) {
        ProgressCard(state = state)
        PrimaryCard(title = "Selection") {
            Text(
                text = "Selected cell",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = selectionValue.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Running value",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = runningValue.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Operations",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onApply(Operation.AddTwo) }
            ) {
                Text(text = "+2")
            }
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onApply(Operation.SubtractThree) }
            ) {
                Text(text = "-3")
            }
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onApply(Operation.MultiplyTwo) }
            ) {
                Text(text = "×2")
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = runningValue % 2 == 0,
                onClick = { onApply(Operation.DivideTwo) }
            ) {
                Text(text = "÷2")
            }
        }
    }
}
