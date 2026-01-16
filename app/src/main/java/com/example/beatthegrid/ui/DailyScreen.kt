package com.example.beatthegrid.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.beatthegrid.GameState
import com.example.beatthegrid.ui.components.PrimaryCard
import com.example.beatthegrid.ui.components.SecondaryCard

@Composable
fun DailyScreen(state: GameState, onStart: () -> Unit) {
    AppScaffold(
        title = "Beat the Grid",
        subtitle = "Day ${state.dayIndex + 1} • Daily Grid"
    ) {
        PrimaryCard(
            title = "Daily target",
            supporting = "Tries left: ${state.triesRemaining} / 5"
        ) {
            Text(
                text = state.target.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        SecondaryCard(title = "Rules") {
            Text(
                text = "Pick a cell, then apply an operation using its number. You get 4 moves. +, -, ×, ÷ operations are allowed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Win by matching the target before time runs out. Each miss costs a daily try.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = state.triesRemaining > 0 && !state.completed,
            onClick = onStart
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = if (state.completed) "Completed" else "Start run")
        }
    }
}
