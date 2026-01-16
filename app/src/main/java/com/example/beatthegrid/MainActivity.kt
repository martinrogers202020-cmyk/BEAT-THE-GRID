package com.example.beatthegrid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.ripple.rememberRipple
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.Locale

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
    val gradient = beatBackgroundGradient()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            BeatTopBar(
                title = "BEAT THE GRID",
                subtitle = "Day ${state.dayIndex + 1} • Daily Grid"
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BeatCard {
                    Text(
                        text = "Daily Target",
                        color = BeatMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TargetPill(value = state.target.toString())
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tries left: ${state.triesRemaining} / 5",
                        color = BeatOnDark
                    )
                }

                BeatCard(
                    containerColor = BeatCardSecondary
                ) {
                    Text(
                        text = "Rules",
                        color = BeatOnDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pick a cell, then apply an operation. You get 4 moves. +2, -3, ×2, ÷2 operations are allowed.",
                        color = BeatMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Win by matching the target before time runs out. Each miss costs a daily try.",
                        color = BeatMuted
                    )
                }

                BeatPrimaryButton(
                    label = if (state.completed) "Completed" else "Start Run",
                    enabled = state.triesRemaining > 0 && !state.completed,
                    onClick = onStart,
                    leadingIcon = { Icon(Icons.Default.ArrowForward, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
fun SelectNumberScreen(state: GameState, onCellSelected: (Int) -> Unit, onBack: () -> Unit) {
    val gradient = beatBackgroundGradient()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            BeatTopBar(
                title = "BEAT THE GRID",
                subtitle = "Pick a cell",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProgressCard(state = state)
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
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
    val gradient = beatBackgroundGradient()
    val runningValue = state.runningValue ?: 0
    val selectionValue = state.selectedIndex?.let { state.grid[it] } ?: 0

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            BeatTopBar(
                title = "BEAT THE GRID",
                subtitle = "Apply operation",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProgressCard(state = state)
            BeatCard {
                Text("Selected cell", color = BeatMuted)
                Text(
                    text = selectionValue.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = BeatOnDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Running value", color = BeatMuted)
                TargetPill(
                    value = runningValue.toString(),
                    modifier = Modifier.padding(top = 6.dp),
                    fontSize = 26.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationButton(
                    label = "+2",
                    colors = listOf(BeatGreen, BeatGreenDeep)
                ) { onApply(Operation.AddTwo) }
                OperationButton(
                    label = "-3",
                    colors = listOf(BeatRed, BeatRedDeep)
                ) { onApply(Operation.SubtractThree) }
                OperationButton(
                    label = "×2",
                    colors = listOf(BeatGreen, BeatGreenDeep)
                ) { onApply(Operation.MultiplyTwo) }
                OperationButton(
                    label = "÷2",
                    enabled = runningValue % 2 == 0,
                    colors = listOf(BeatBlue, BeatBlueDeep)
                ) { onApply(Operation.DivideTwo) }
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
    val gradient = beatBackgroundGradient()
    val resultText = if (state.result == AttemptOutcome.Won) "Victory" else "Out of moves"
    val shareMessage = buildShareText(state)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            BeatTopBar(
                title = "BEAT THE GRID",
                subtitle = "Results",
                onBack = onHome,
                actions = {
                    IconButton(onClick = { onShare(shareMessage) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = BeatOnDark)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BeatCard {
                Text(resultText, color = BeatOnDark, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Target",
                    color = BeatMuted
                )
                TargetPill(
                    value = state.target.toString(),
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Final value: ${state.runningValue ?: 0}",
                    color = BeatOnDark
                )
                Text(
                    text = "Moves used: ${state.moveIndex} / 4",
                    color = BeatMuted
                )
            }

            Text(
                text = "Used grid",
                color = BeatOnDark,
                fontWeight = FontWeight.SemiBold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                gridTiles(
                    grid = state.grid,
                    usedIndices = state.usedIndices,
                    selectedIndex = null,
                    enabled = false,
                    onCellSelected = {}
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BeatSecondaryButton(
                    label = "Try again",
                    enabled = state.triesRemaining > 0 && state.result == AttemptOutcome.Lost,
                    modifier = Modifier.weight(1f),
                    onClick = onPlayAgain
                )
                BeatTertiaryButton(
                    label = "Daily",
                    modifier = Modifier.weight(1f),
                    onClick = onHome
                )
            }
        }
    }
}

@Composable
fun ProgressCard(state: GameState) {
    BeatCard(
        containerColor = BeatCardSecondary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Target",
                    color = BeatMuted,
                    fontWeight = FontWeight.SemiBold
                )
                TargetPill(
                    value = state.target.toString(),
                    modifier = Modifier.padding(top = 6.dp),
                    fontSize = 18.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Move", color = BeatMuted)
                Text(
                    text = "${state.moveIndex + 1} / 4",
                    color = BeatOnDark,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Running", color = BeatMuted)
                Text(
                    text = (state.runningValue ?: 0).toString(),
                    color = BeatOnDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Divider(modifier = Modifier.padding(vertical = 12.dp), color = BeatOutline)
        Text(
            text = "Tries left: ${state.triesRemaining} / 5",
            color = BeatMuted
        )
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
fun BeatTopBar(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BeatOnDark)
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayMedium,
                    color = BeatOnDark
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = BeatMuted
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

@Composable
fun BeatCard(
    modifier: Modifier = Modifier,
    containerColor: Color = BeatCard,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun TargetPill(
    value: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 32.sp
) {
    val pillGradient = Brush.horizontalGradient(listOf(BeatTarget, BeatTargetDeep))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(pillGradient)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            color = Color(0xFF2B1300),
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold
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
    val shape = RoundedCornerShape(12.dp)
    val brush = when (state) {
        TileState.Available -> Brush.linearGradient(listOf(BeatTileHighlight, BeatTileBase))
        TileState.Used -> Brush.linearGradient(listOf(BeatTileUsed, BeatTileUsedDark))
        TileState.Selected -> Brush.linearGradient(listOf(BeatGreen, BeatGreenDeep))
    }
    val borderColor = when (state) {
        TileState.Selected -> BeatGreen
        TileState.Used -> BeatOutline
        TileState.Available -> BeatOutline
    }
    val textColor = when (state) {
        TileState.Selected -> Color(0xFF062B1A)
        TileState.Used -> BeatMuted
        TileState.Available -> BeatOnDark
    }

    PressableSurface(
        modifier = Modifier.aspectRatio(1f),
        enabled = enabled,
        shape = shape,
        brush = brush,
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(4.dp)
                    .border(
                        BorderStroke(1.dp, BeatTileEdgeHighlight.copy(alpha = 0.35f)),
                        RoundedCornerShape(10.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(2.dp)
                    .border(
                        BorderStroke(1.dp, BeatTileEdgeShadow.copy(alpha = 0.5f)),
                        RoundedCornerShape(11.dp)
                    )
            )
            Text(
                text = value.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun OperationButton(
    label: String,
    colors: List<Color>,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val brush = if (enabled) {
        Brush.verticalGradient(colors)
    } else {
        Brush.verticalGradient(listOf(BeatDisabledButton, BeatDisabledButtonDeep))
    }
    PressableSurface(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        brush = brush,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        onClick = onClick,
        rippleColor = Color.White
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = if (enabled) Color.White else BeatMuted
        )
    }
}

@Composable
fun BeatPrimaryButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val brush = Brush.linearGradient(listOf(BeatBlue, BeatBlueDeep))
    PressableSurface(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        brush = brush,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        onClick = onClick,
        rippleColor = Color.White
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun BeatSecondaryButton(
    label: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val brush = Brush.linearGradient(listOf(BeatBlue, BeatBlueDeep))
    PressableSurface(
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        brush = brush,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        onClick = onClick,
        rippleColor = Color.White
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun BeatTertiaryButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val brush = Brush.linearGradient(listOf(BeatCardSecondary, BeatCard))
    PressableSurface(
        modifier = modifier,
        enabled = true,
        shape = RoundedCornerShape(18.dp),
        brush = brush,
        border = BorderStroke(1.dp, BeatOutline),
        onClick = onClick,
        rippleColor = Color.White.copy(alpha = 0.6f)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = BeatOnDark
        )
    }
}

@Composable
fun PressableSurface(
    modifier: Modifier,
    enabled: Boolean,
    shape: RoundedCornerShape,
    brush: Brush,
    border: BorderStroke? = null,
    rippleColor: Color = Color.White,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        label = "press-scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.7f
            }
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .background(brush, shape)
            .clip(shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = true,
                    color = rippleColor
                ),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}
