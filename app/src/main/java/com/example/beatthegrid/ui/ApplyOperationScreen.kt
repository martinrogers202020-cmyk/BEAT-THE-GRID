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
    val runningValue = state.runningValue
    val selectionValue = state.selectedIndex?.let { state.grid[it] } ?: 0
    val canDivide = selectionValue != 0 && runningValue % selectionValue == 0

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
                text = "Start: ${state.startValue}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Current: $runningValue",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Target: ${state.target}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                onClick = { onApply(Operation.Add) }
            ) {
                Text(text = "+")
            }
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onApply(Operation.Subtract) }
            ) {
                Text(text = "−")
            }
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onApply(Operation.Multiply) }
            ) {
                Text(text = "×")
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = canDivide,
                onClick = { onApply(Operation.Divide) }
            ) {
                Text(text = "÷")
            }
        }
    }
}
