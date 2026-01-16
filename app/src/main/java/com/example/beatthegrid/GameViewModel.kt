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

    fun selectCell(index: Int) {
        val current = _state.value
        if (current.usedIndices.contains(index) || current.result != AttemptOutcome.InProgress) return
        val running = current.runningValue ?: 0
        _state.value = current.copy(selectedIndex = index, runningValue = running)
    }

    fun applyOperation(operation: Operation): AttemptResult {
        val current = _state.value
        val selected = current.selectedIndex ?: return AttemptResult.Continue
        val running = current.runningValue ?: 0
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

        _state.value = current.copy(
            runningValue = updatedValue,
            moveIndex = newMoveIndex,
            usedIndices = used,
            selectedIndex = null,
            result = result,
            completed = result != AttemptOutcome.InProgress || current.completed
        )

        if (result != AttemptOutcome.InProgress) {
            viewModelScope.launch {
                store.edit { prefs ->
                    val updatedTries = if (result == AttemptOutcome.Lost) (current.triesRemaining - 1).coerceAtLeast(0)
                    else current.triesRemaining
                    prefs[triesKey] = updatedTries
                    prefs[completedKey] = result == AttemptOutcome.Won || updatedTries == 0
                    prefs[dayKey] = dayIndex
                }
            }
            _state.value = _state.value.copy(
                triesRemaining = if (result == AttemptOutcome.Lost) (current.triesRemaining - 1).coerceAtLeast(0) else current.triesRemaining,
                completed = result == AttemptOutcome.Won || (current.triesRemaining - 1) <= 0 && result == AttemptOutcome.Lost
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
    val grid: List<Int>,
    val target: Int,
    val runningValue: Int?,
    val moveIndex: Int,
    val usedIndices: Set<Int>,
    val selectedIndex: Int?,
    val triesRemaining: Int,
    val completed: Boolean,
    val result: AttemptOutcome
) {
    fun resetForAttempt(): GameState {
        return copy(
            runningValue = 0,
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
                grid = emptyList(),
                target = 0,
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
            val random = Random(dayIndex.toLong())
            val grid = List(36) { random.nextInt(1, 13) }
            val target = random.nextInt(10, 81)
            return GameState(
                dayIndex = dayIndex,
                grid = grid,
                target = target,
                runningValue = 0,
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

fun calculateDayIndex(): Int {
    val start = LocalDate.of(2025, 1, 1)
    val now = LocalDate.now()
    return ChronoUnit.DAYS.between(start, now).toInt().coerceAtLeast(0)
}
