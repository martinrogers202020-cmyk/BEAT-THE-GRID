package com.example.beatthegrid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.Locale

private val MaxContentWidth = 480.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeatTheGridApp()
        }
    }
}

@Composable
fun BeatTheGridApp(viewModel: GameViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    BeatTheGridTheme {
        NavHost(
            navController = navController,
            startDestination = Screen.Daily.route
        ) {
            composable(Screen.Daily.route) {
                DailyScreen(state = state, onStart = {
                    viewModel.startAttempt()
                    navController.navigate(Screen.SelectNumber.route)
                })
            }
            composable(Screen.SelectNumber.route) {
                SelectNumberScreen(
                    state = state,
                    onCellSelected = { index ->
                        viewModel.selectCell(index)
                        navController.navigate(Screen.ApplyOperation.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ApplyOperation.route) {
                ApplyOperationScreen(
                    state = state,
                    onApply = { operation ->
                        val result = viewModel.applyOperation(operation)
                        if (result is AttemptResult.Finished) {
                            navController.navigate(Screen.Results.route) {
                                popUpTo(Screen.Daily.route) { inclusive = false }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Results.route) {
                ResultsScreen(
                    state = state,
                    onShare = { shareText ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        val chooser = Intent.createChooser(intent, "Share results")
                        if (context !is Activity) {
                            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(chooser)
                    },
                    onPlayAgain = {
                        viewModel.startAttempt()
                        navController.navigate(Screen.SelectNumber.route)
                    },
                    onHome = {
                        navController.navigate(Screen.Daily.route) {
                            popUpTo(Screen.Daily.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

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
                text = "Pick a cell, then apply an operation. You get 4 moves. +2, -3, ×2, ÷2 operations are allowed.",
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

@Composable
fun SelectNumberScreen(state: GameState, onCellSelected: (Int) -> Unit, onBack: () -> Unit) {
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
    }
}

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
                text = "Final value: ${state.runningValue ?: 0}",
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

@Composable
fun ProgressCard(state: GameState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Target",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.target.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Move",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${state.moveIndex + 1} / 4",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Running",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = (state.runningValue ?: 0).toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = "Tries left: ${state.triesRemaining} / 5",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun buildShareText(state: GameState): String {
    val status = if (state.result == AttemptOutcome.Won) "WIN" else "LOSE"
    val dayLabel = String.format(Locale.US, "%d", state.dayIndex + 1)
    return "Beat the Grid Day $dayLabel: $status. Target ${state.target}, final ${state.runningValue ?: 0}. Moves ${state.moveIndex}/4."
}

sealed class Screen(val route: String) {
    data object Daily : Screen("daily")
    data object SelectNumber : Screen("select")
    data object ApplyOperation : Screen("apply")
    data object Results : Screen("results")
}

enum class TileState {
    Available,
    Used,
    Selected
}

private fun LazyGridScope.gridTiles(
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
fun AppScaffold(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .widthIn(max = MaxContentWidth),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
fun PrimaryCard(
    title: String,
    supporting: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SecondaryCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
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
