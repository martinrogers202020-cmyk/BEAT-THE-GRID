package com.example.beatthegrid

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

private val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = "beat_the_grid")

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val store = application.dataStore
    private val dayIndex = calculateDayIndex()

    private val _state = MutableStateFlow(GameState.empty(dayIndex))
    val state: StateFlow<GameState> = _state

    private val triesKey = intPreferencesKey("tries_remaining")
    private val dayKey = intPreferencesKey("day_index")
    private val completedKey = booleanPreferencesKey("completed")

    init {
        viewModelScope.launch {
            val prefs = store.data.first()
            val storedDay = prefs[dayKey] ?: dayIndex
            val isNewDay = storedDay != dayIndex
            val triesRemaining = if (isNewDay) 5 else (prefs[triesKey] ?: 5)
            val completed = if (isNewDay) false else (prefs[completedKey] ?: false)

            if (isNewDay) {
                store.edit { editable ->
                    editable[dayKey] = dayIndex
                    editable[triesKey] = 5
                    editable[completedKey] = false
                }
            }

            _state.value = GameState.newDaily(dayIndex, triesRemaining, completed)
        }
    }

    fun startAttempt() {
        val current = _state.value
        if (current.completed || current.triesRemaining <= 0) return
        _state.value = current.resetForAttempt()
    }

    fun advanceLevel() {
        val current = _state.value
        val nextLevelIndex = current.levelIndex + 1
        val level = generateLevel(dayIndex, nextLevelIndex)
        _state.value = current.copy(
            levelIndex = nextLevelIndex,
            grid = level.grid,
            target = level.target,
            startValue = level.startValue,
            runningValue = level.startValue,
            moveIndex = 0,
            usedIndices = emptySet(),
            selectedIndex = null,
            result = AttemptOutcome.InProgress
        )
    }

    fun resetAttemptState() {
        val current = _state.value
        _state.value = current.copy(
            runningValue = current.startValue,
            moveIndex = 0,
            usedIndices = emptySet(),
            selectedIndex = null,
            result = AttemptOutcome.InProgress
        )
    }

    fun selectCell(index: Int) {
        val current = _state.value
        if (current.usedIndices.contains(index) || current.result != AttemptOutcome.InProgress) return
        _state.value = current.copy(selectedIndex = index)
    }

    fun applyOperation(operation: Operation): AttemptResult {
        val current = _state.value
        val selected = current.selectedIndex ?: return AttemptResult.Continue
        val running = current.runningValue
        val tileValue = current.grid[selected]
        if (operation == Operation.Divide && (tileValue == 0 || running % tileValue != 0)) {
            return AttemptResult.Continue
        }
        val updatedValue = when (operation) {
            Operation.Add -> running + tileValue
            Operation.Subtract -> running - tileValue
            Operation.Multiply -> running * tileValue
            Operation.Divide -> running / tileValue
        }

        val newMoveIndex = current.moveIndex + 1
        val used = current.usedIndices + selected
        val win = updatedValue == current.target
        val loss = !win && newMoveIndex >= 4
        val result = when {
            win -> AttemptOutcome.Won
            loss -> AttemptOutcome.Lost
            else -> AttemptOutcome.InProgress
        }

        val updatedState = current.copy(
            runningValue = updatedValue,
            moveIndex = newMoveIndex,
            usedIndices = used,
            selectedIndex = null,
            result = result
        )
        _state.value = updatedState

        if (result != AttemptOutcome.InProgress) {
            viewModelScope.launch {
                store.edit { prefs ->
                    val updatedTries = if (result == AttemptOutcome.Lost) {
                        (current.triesRemaining - 1).coerceAtLeast(0)
                    } else {
                        current.triesRemaining
                    }
                    val completed = updatedTries == 0
                    prefs[triesKey] = updatedTries
                    prefs[completedKey] = completed
                    prefs[dayKey] = dayIndex
                }
            }
            val updatedTries = if (result == AttemptOutcome.Lost) {
                (current.triesRemaining - 1).coerceAtLeast(0)
            } else {
                current.triesRemaining
            }
            _state.value = _state.value.copy(
                triesRemaining = updatedTries,
                completed = updatedTries == 0
            )
            return AttemptResult.Finished
        }

        return AttemptResult.Continue
    }
}

enum class Operation {
    Add,
    Subtract,
    Multiply,
    Divide
}

sealed class AttemptResult {
    data object Continue : AttemptResult()
    data object Finished : AttemptResult()
}

enum class AttemptOutcome {
    InProgress,
    Won,
    Lost
}

data class GameState(
    val dayIndex: Int,
    val levelIndex: Int,
    val grid: List<Int>,
    val target: Int,
    val startValue: Int,
    val runningValue: Int,
    val moveIndex: Int,
    val usedIndices: Set<Int>,
    val selectedIndex: Int?,
    val triesRemaining: Int,
    val completed: Boolean,
    val result: AttemptOutcome
) {
    fun resetForAttempt(): GameState {
        return copy(
            runningValue = startValue,
            moveIndex = 0,
            usedIndices = emptySet(),
            selectedIndex = null,
            result = AttemptOutcome.InProgress
        )
    }

    companion object {
        fun empty(dayIndex: Int): GameState {
            return GameState(
                dayIndex = dayIndex,
                levelIndex = 1,
                grid = emptyList(),
                target = 0,
                startValue = 0,
                runningValue = 0,
                moveIndex = 0,
                usedIndices = emptySet(),
                selectedIndex = null,
                triesRemaining = 5,
                completed = false,
                result = AttemptOutcome.InProgress
            )
        }

        fun newDaily(dayIndex: Int, triesRemaining: Int, completed: Boolean): GameState {
            val level = generateLevel(dayIndex, levelIndex = 1)
            return GameState(
                dayIndex = dayIndex,
                levelIndex = 1,
                grid = level.grid,
                target = level.target,
                startValue = level.startValue,
                runningValue = level.startValue,
                moveIndex = 0,
                usedIndices = emptySet(),
                selectedIndex = null,
                triesRemaining = triesRemaining,
                completed = completed,
                result = AttemptOutcome.InProgress
            )
        }
    }
}

private data class LevelData(
    val grid: List<Int>,
    val target: Int,
    val startValue: Int
)

private fun generateLevel(dayIndex: Int, levelIndex: Int): LevelData {
    val seed = (dayIndex.toLong() shl 32) + levelIndex.toLong()
    val random = Random(seed)
    val startMax = (299 + (levelIndex - 1) * 120).coerceAtMost(999)
    val startMin = 100
    val startValue = random.nextInt(startMin, startMax + 1)
    val gridMax = (9 + (levelIndex - 1)).coerceAtMost(12)
    val gridMin = if (levelIndex > 5) 0 else 1
    val grid = List(36) { random.nextInt(gridMin, gridMax + 1) }
    val target = random.nextInt(1, 10)
    return LevelData(grid = grid, target = target, startValue = startValue)
}

fun calculateDayIndex(): Int {
    val start = LocalDate.of(2025, 1, 1)
    val now = LocalDate.now()
    return ChronoUnit.DAYS.between(start, now).toInt().coerceAtLeast(0)
}
