package com.edd.orange.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edd.orange.model.GameStatus
import com.edd.orange.model.Orange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GameViewModel @Inject constructor() : ViewModel() {

    sealed class GameIntent {
        data object StartGame : GameIntent()
        data class EndGame(val finalScore: Int) : GameIntent()
        data class StartDrag(val x: Float, val y: Float) : GameIntent()
        data class UpdateDrag(val x: Float, val y: Float) : GameIntent()
        data object EndDrag : GameIntent()
    }

    data class GameUiState(
        val gameStatus: GameStatus = GameStatus.WAITING,
        val score: Int = 0,
        val oranges: List<Orange> = emptyList(),
        val remainingTime: Long = 0L,
        val isDragging: Boolean = false,
        val dragStart: Pair<Float, Float>? = null,
        val currentDrag: Pair<Float, Float>? = null,
        val selectedOranges: Set<Int> = emptySet()
    )

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()


    fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.StartGame -> startGame()
            is GameIntent.EndGame -> endGame(intent.finalScore)
            is GameIntent.StartDrag -> startDrag(intent.x, intent.y)
            is GameIntent.UpdateDrag -> updateDrag(intent.x, intent.y)
            is GameIntent.EndDrag -> endDrag()
        }
    }


    private fun startGame() {
        _uiState.value = GameUiState(
            gameStatus = GameStatus.IN_PROGRESS,
            oranges = generateOranges(),
            remainingTime = GAME_DURATION,
            score = 0
        )

        startTimer()
    }

    private fun endGame(finalScore: Int) {
        _uiState.update { state ->
            state.copy(
                gameStatus = GameStatus.GAME_OVER,
                score = finalScore,
                isDragging = false,
                dragStart = null,
                currentDrag = null,
                selectedOranges = emptySet()
            )
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_uiState.value.remainingTime > 0) {
                _uiState.update { state -> state.copy(remainingTime = state.remainingTime - 1000) }
                delay(1000)
            }

            handleIntent(GameIntent.EndGame(_uiState.value.score))
        }
    }

    private fun startDrag(startX: Float, startY: Float) {
        _uiState.update { state ->
            state.copy(
                isDragging = true,
                dragStart = startX to startY,
                currentDrag = startX to startY,
                selectedOranges = emptySet()
            )
        }
    }

    private fun updateDrag(currentX: Float, currentY: Float) {
        _uiState.update { state ->
            state.copy(
                currentDrag = currentX to currentY,
                selectedOranges = computeSelectedOranges(state.oranges, state.dragStart, currentX to currentY)
            )
        }
    }

    private fun endDrag() {
        var additionalScore = _uiState.value.selectedOranges.size
        val selectedOrangeSum = _uiState.value.selectedOranges.sumOf {
            _uiState.value.oranges[it].value
        }

        if (selectedOrangeSum != SUM_TARGET) {
            additionalScore = 0
        }
        val updatedOranges = _uiState.value.oranges.mapIndexed { index, orange ->
            if (index in _uiState.value.selectedOranges) {
                orange.copy(removed = selectedOrangeSum == SUM_TARGET)
            } else {
                orange
            }
        }

        _uiState.update { state ->
            state.copy(
                isDragging = false,
                dragStart = null,
                currentDrag = null,
                oranges = updatedOranges,
                score = state.score + additionalScore,
                selectedOranges = emptySet(),
            )
        }
    }

    private fun computeSelectedOranges(
        oranges: List<Orange>,
        dragStart: Pair<Float, Float>?,
        current: Pair<Float, Float>?
    ): Set<Int> {
        if (dragStart == null || current == null) {
            return emptySet()
        }

        val left = minOf(dragStart.first, current.first)
        val right = maxOf(dragStart.first, current.first)
        val top = minOf(dragStart.second, current.second)
        val bottom = maxOf(dragStart.second, current.second)

        return oranges.mapIndexedNotNull { index, orange ->
            if (!orange.removed && orange.x in left..right && orange.y in top..bottom) index
            else null
        }.toSet()
    }

    private fun generateOranges(): List<Orange> {
        val values = mutableListOf<Int>()
        values.addAll(List(32) { 5 })
        for (i in 1..9) {
            if (i != 5) {
                values.addAll(List(16) { i })
            }
        }

        values.shuffle()

        return buildList {
            (0 until 10).forEach { row ->
                (0 until 16).forEach { col ->
                    add(Orange(values[row * 10 + col]))
                }
            }
        }
    }


    companion object {
        private const val GAME_DURATION = 120_000L
        private const val SUM_TARGET = 10
    }
}