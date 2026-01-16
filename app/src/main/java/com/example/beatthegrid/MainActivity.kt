package com.example.beatthegrid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BeatCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 12.dp
                ) {
                    Text(
                        text = "Daily Target",
                        color = BeatMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TargetPill(
                        value = state.target.toString(),
                        fontSize = 52.sp,
                        colors = listOf(BeatTarget, BeatTargetDeep, BeatTargetDeep),
                        shadowElevation = 14.dp,
                        shadowColor = BeatTargetDeep
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tries left: ${state.triesRemaining} / 5",
                        color = BeatMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                BeatCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = BeatCardSecondary.copy(alpha = 0.9f),
                    contentPadding = 16.dp
                ) {
                    Text(
                        text = "Rules",
                        color = BeatOnDark,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pick a cell, then apply an operation. You get 4 moves. +2, -3, ×2, ÷2 operations are allowed.",
                        color = BeatMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Win by matching the target before time runs out. Each miss costs a daily try.",
                        color = BeatMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                BeatPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
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
    val trayShape = RoundedCornerShape(30.dp)
    val trayBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF171C2D),
            Color(0xFF0B0F1A)
        )
    )

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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressCard(
                state = state,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = trayShape,
                        ambientColor = Color.Black.copy(alpha = 0.7f),
                        spotColor = Color.Black.copy(alpha = 0.85f)
                    )
                    .background(trayBrush, trayShape)
                    .border(BorderStroke(2.dp, Color(0xFF2B3450)), trayShape)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(trayShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(280f, 240f)
                            )
                        )
                )
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

            GridTray(modifier = Modifier.fillMaxWidth()) {
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
fun ProgressCard(state: GameState, modifier: Modifier = Modifier) {
    BeatHudCard(modifier = modifier) {
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
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BeatHudPanel, RoundedCornerShape(20.dp))
                        .border(BorderStroke(1.dp, BeatHudBorder), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = title,
                        color = BeatOnDark,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = subtitle,
                        color = BeatOnDark.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp
                    )
                }
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
    contentPadding: Dp = 20.dp,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun BeatHudCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 18.dp,
    cornerRadius: Dp = 22.dp,
    elevation: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(BeatCardSecondary, BeatCard)
                ),
                shape
            )
            .border(BorderStroke(1.dp, BeatHudBorder), shape)
            .clip(shape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        start = Offset.Zero,
                        end = Offset(300f, 220f)
                    )
                )
        )
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun GridTray(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(BeatGridTrayTop, BeatGridTrayBottom)
                ),
                shape
            )
            .border(BorderStroke(1.dp, BeatGridTrayBorder), shape)
            .clip(shape)
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
fun TargetPill(
    value: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 32.sp,
    colors: List<Color> = listOf(BeatTarget, BeatTargetDeep),
    shadowElevation: Dp = 0.dp,
    shadowColor: Color = BeatTargetDeep
) {
    val pillGradient = Brush.horizontalGradient(colors)
    Box(
        modifier = modifier
            .then(
                if (shadowElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = shadowElevation,
                        shape = RoundedCornerShape(999.dp),
                        ambientColor = shadowColor.copy(alpha = 0.6f),
                        spotColor = shadowColor.copy(alpha = 0.7f)
                    )
                } else {
                    Modifier
                }
            )
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
    val shape = RoundedCornerShape(24.dp)
    val brush = when (state) {
        TileState.Available -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF69C5FF),
                Color(0xFF2A7CFF),
                Color(0xFF0B3C8A)
            ),
            start = Offset(0f, 0f),
            end = Offset(320f, 340f)
        )
        TileState.Used -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF4A5868).copy(alpha = 0.7f),
                Color(0xFF2C3947).copy(alpha = 0.7f)
            ),
            start = Offset(0f, 0f),
            end = Offset(260f, 280f)
        )
        TileState.Selected -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF73FF9D),
                Color(0xFF22E86D),
                Color(0xFF0C9E4A)
            ),
            start = Offset(0f, 0f),
            end = Offset(320f, 340f)
        )
    }
    val borderColor = when (state) {
        TileState.Selected -> Color(0xFFB2FFD1).copy(alpha = 0.95f)
        TileState.Used -> Color(0xFF4C5B6C).copy(alpha = 0.45f)
        TileState.Available -> Color(0xFF9FDBFF).copy(alpha = 0.7f)
    }
    val textColor = when (state) {
        TileState.Selected -> Color(0xFF053117)
        TileState.Used -> Color(0xFF91A0B2)
        TileState.Available -> Color(0xFFF5FBFF)
    }
    val shadowElevation = when (state) {
        TileState.Selected -> 26.dp
        TileState.Available -> 18.dp
        TileState.Used -> 0.dp
    }
    val shadowColor = when (state) {
        TileState.Selected -> Color(0xFF3CFF8D)
        TileState.Available -> Color(0xFF1F6BFF)
        TileState.Used -> Color.Transparent
    }
    val highlightColor = when (state) {
        TileState.Selected -> Color(0xFFD6FFE7).copy(alpha = 0.8f)
        TileState.Used -> Color.White.copy(alpha = 0.06f)
        TileState.Available -> Color.White.copy(alpha = 0.35f)
    }
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appeared = true
    }
    val tileScale by animateFloatAsState(
        targetValue = if (state == TileState.Selected) 1.04f else 1f,
        animationSpec = tween(durationMillis = 160),
        label = "tile-scale"
    )
    val numberScale by animateFloatAsState(
        targetValue = when {
            !appeared -> 0.92f
            state == TileState.Selected -> 1.08f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 140),
        label = "tile-number-scale"
    )

    PressableSurface(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = tileScale
                scaleY = tileScale
            },
        enabled = enabled,
        shape = shape,
        brush = brush,
        border = BorderStroke(if (state == TileState.Selected) 3.dp else 2.dp, borderColor),
        shadowElevation = shadowElevation,
        shadowColor = shadowColor,
        pressedScale = 0.94f,
        disabledAlpha = if (state == TileState.Used) 0.35f else 0.7f,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (state == TileState.Selected) 0.22f else 0.14f),
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(highlightColor, Color.Transparent)
                        )
                    )
            )
            if (state == TileState.Selected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(3.dp)
                        .border(
                            BorderStroke(2.dp, Color(0xFFB9FFDA).copy(alpha = 0.75f)),
                            RoundedCornerShape(20.dp)
                        )
                )
            }
            if (state != TileState.Used) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(4.dp)
                        .border(
                            BorderStroke(1.dp, Color(0xFF061B38).copy(alpha = 0.55f)),
                            RoundedCornerShape(20.dp)
                        )
                )
            }
            Text(
                text = value.toString(),
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                color = textColor,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.35f),
                        offset = Offset(0f, 2f),
                        blurRadius = 6f
                    )
                ),
                modifier = Modifier.graphicsLayer {
                    scaleX = numberScale
                    scaleY = numberScale
                }
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
    modifier: Modifier = Modifier,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val brush = if (enabled) {
        Brush.verticalGradient(listOf(BeatBlue, BeatBlueDeep))
    } else {
        Brush.verticalGradient(
            listOf(
                BeatBlue.copy(alpha = 0.35f),
                BeatBlueDeep.copy(alpha = 0.45f)
            )
        )
    }
    PressableSurface(
        modifier = modifier.height(58.dp),
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        brush = brush,
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (enabled) 0.18f else 0.08f)),
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
    shadowElevation: Dp = 8.dp,
    shadowColor: Color = Color.Black,
    pressedScale: Float = 0.95f,
    disabledAlpha: Float = 0.7f,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = tween(durationMillis = 90),
        label = "press-scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else disabledAlpha
            }
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = shadowColor.copy(alpha = 0.35f),
                spotColor = shadowColor.copy(alpha = 0.55f)
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
