package com.example.beatthegrid.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.beatthegrid.AttemptResult
import com.example.beatthegrid.BeatTheGridTheme
import com.example.beatthegrid.GameViewModel

@Composable
fun BeatTheGridApp(viewModel: GameViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Daily.route

    BeatTheGridTheme {
        AnimatedContent(
            targetState = currentRoute,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 10 }) togetherWith
                    (fadeOut(animationSpec = tween(220)) + slideOutHorizontally(animationSpec = tween(220)) { -it / 10 })
            },
            label = "navigation"
        ) { _ ->
            NavHost(
                navController = navController,
                startDestination = Screen.Daily.route
            ) {
                composable(Screen.Daily.route) {
                    val state by viewModel.state.collectAsState()
                    DailyScreen(state = state, onStart = {
                        viewModel.startAttempt()
                        navController.navigate(Screen.SelectNumber.route)
                    })
                }
                composable(Screen.SelectNumber.route) {
                    val state by viewModel.state.collectAsState()
                    SelectNumberScreen(
                        state = state,
                        onCellSelected = { index ->
                            viewModel.selectCell(index)
                            navController.navigate(Screen.ApplyOperation.route)
                        },
                        onNextLevel = {
                            viewModel.advanceLevel()
                        },
                        onRetry = {
                            viewModel.startAttempt()
                        },
                        onHome = {
                            viewModel.resetAttemptState()
                            navController.navigate(Screen.Daily.route) {
                                popUpTo(Screen.Daily.route) { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.ApplyOperation.route) {
                    val state by viewModel.state.collectAsState()
                    ApplyOperationScreen(
                        state = state,
                        onApply = { operation ->
                            val result = viewModel.applyOperation(operation)
                            if (result is AttemptResult.Finished) {
                                navController.navigate(Screen.Results.route) {
                                    launchSingleTop = true
                                }
                            } else {
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Results.route) {
                    val state by viewModel.state.collectAsState()
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
                            navController.navigate(Screen.SelectNumber.route) {
                                launchSingleTop = true
                            }
                        },
                        onNextLevel = {
                            viewModel.startAttempt()
                            navController.navigate(Screen.SelectNumber.route) {
                                launchSingleTop = true
                            }
                        },
                        onRetry = {
                            viewModel.startAttempt()
                            navController.navigate(Screen.SelectNumber.route) {
                                launchSingleTop = true
                            }
                        },
                        onHome = {
                            navController.navigate(Screen.Daily.route) {
                                popUpTo(Screen.Daily.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
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
