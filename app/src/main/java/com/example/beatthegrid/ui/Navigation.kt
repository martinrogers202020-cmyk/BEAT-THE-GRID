package com.example.beatthegrid.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.beatthegrid.AttemptResult
import com.example.beatthegrid.BeatTheGridTheme
import com.example.beatthegrid.GameViewModel

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

sealed class Screen(val route: String) {
    data object Daily : Screen("daily")
    data object SelectNumber : Screen("select")
    data object ApplyOperation : Screen("apply")
    data object Results : Screen("results")
}
