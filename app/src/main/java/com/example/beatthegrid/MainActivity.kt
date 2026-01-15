package com.example.beatthegrid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
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
                            startActivity(Intent.createChooser(intent, "Share results"))
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
}

@Composable
fun DailyScreen(state: GameState, onStart: () -> Unit) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0B1220), Color(0xFF1F2A44), Color(0xFF111827))
    )

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Daily Grid") },
            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF111827),
                titleContentColor = Color.White
            )
        )
    }) { padding ->
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Beat the Grid",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Day ${state.dayIndex + 1}",
                            color = Color(0xFF9CA3AF)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Target",
                            color = Color(0xFF9CA3AF)
                        )
                        Text(
                            text = state.target.toString(),
                            color = Color(0xFFFBBF24),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tries left: ${state.triesRemaining} / 5",
                            color = Color.White
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Rules",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pick a cell, then apply an operation. You get 4 moves. +2, -3, ×2, ÷2 operations are allowed.",
                            color = Color(0xFF9CA3AF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Win by matching the target before time runs out. Each miss costs a daily try.",
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                Button(
                    onClick = onStart,
                    enabled = state.triesRemaining > 0 && !state.completed,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (state.completed) "Completed" else "Start Run",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SelectNumberScreen(state: GameState, onCellSelected: (Int) -> Unit, onBack: () -> Unit) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF111827), Color(0xFF1F2937), Color(0xFF0F172A))
    )

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Pick a cell") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Refresh, contentDescription = "Back")
                }
            },
            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF111827),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
    }) { padding ->
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
                itemsIndexed(state.grid) { index, value ->
                    val used = state.usedIndices.contains(index)
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (used) Color(0xFF374151) else Color(0xFF1F2937)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Button(
                            onClick = { onCellSelected(index) },
                            enabled = !used,
                            modifier = Modifier.fillMaxSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (used) Color(0xFF374151) else Color(0xFF2563EB),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = value.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplyOperationScreen(state: GameState, onApply: (Operation) -> Unit, onBack: () -> Unit) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
    )
    val runningValue = state.runningValue ?: 0
    val selectionValue = state.selectedIndex?.let { state.grid[it] } ?: 0

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Apply Operation") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Refresh, contentDescription = "Back")
                }
            },
            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF111827),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProgressCard(state = state)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selected cell", color = Color(0xFF9CA3AF))
                    Text(
                        text = selectionValue.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Running value", color = Color(0xFF9CA3AF))
                    Text(
                        text = runningValue.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationButton(label = "+2", enabled = true) { onApply(Operation.AddTwo) }
                OperationButton(label = "-3", enabled = true) { onApply(Operation.SubtractThree) }
                OperationButton(label = "×2", enabled = true) { onApply(Operation.MultiplyTwo) }
                OperationButton(
                    label = "÷2",
                    enabled = runningValue % 2 == 0
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
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF111827), Color(0xFF0F172A))
    )
    val resultText = if (state.result == AttemptOutcome.Won) "Victory" else "Out of moves"
    val shareMessage = buildShareText(state)

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Results") },
            actions = {
                IconButton(onClick = { onShare(shareMessage) }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            },
            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF111827),
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(resultText, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Target: ${state.target}",
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = "Final value: ${state.runningValue ?: 0}",
                        color = Color(0xFFFBBF24)
                    )
                    Text(
                        text = "Moves used: ${state.moveIndex} / 4",
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Text(
                text = "Used grid",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(state.grid) { index, value ->
                    val used = state.usedIndices.contains(index)
                    Card(
                        modifier = Modifier.aspectRatio(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (used) Color(0xFF22C55E) else Color(0xFF1F2937)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = value.toString(),
                                color = if (used) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPlayAgain,
                    enabled = state.triesRemaining > 0 && state.result == AttemptOutcome.Lost,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Try again")
                }
                Button(
                    onClick = onHome,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563))
                ) {
                    Text("Daily")
                }
            }
        }
    }
}

@Composable
fun ProgressCard(state: GameState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Target ${state.target}",
                color = Color(0xFFFBBF24),
                fontWeight = FontWeight.Bold
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF374151))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Move", color = Color(0xFF9CA3AF))
                    Text(
                        text = "${state.moveIndex + 1} / 4",
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Running", color = Color(0xFF9CA3AF))
                    Text(
                        text = (state.runningValue ?: 0).toString(),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun OperationButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
    ) {
        Text(label, fontWeight = FontWeight.Bold)
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
