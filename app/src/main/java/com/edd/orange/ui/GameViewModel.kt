package com.edd.orange.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.edd.orange.model.GameState
import com.edd.orange.model.GameStatus
import com.edd.orange.model.Orange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor() : ViewModel() {

    sealed class GameIntent {
        data object StartGame : GameIntent()
        data class OnDragStart(val startX: Float, val startY: Float) : GameIntent()

        data class OnDragEnd(val endX: Float, val endY: Float) : GameIntent()
    }


    private val _gameStateFlow = MutableStateFlow(GameState())
    val gameStateFlow = _gameStateFlow.asStateFlow()


    fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.StartGame -> startGame()
            is GameIntent.OnDragStart -> onDragStart(intent.startX, intent.startY)
            is GameIntent.OnDragEnd -> onDragEnd(intent.endX, intent.endY)

        }
    }

    private fun startGame() {
        _gameStateFlow.value = GameState(
            oranges = generateOranges(),
            score = 0,
            gameStatus = GameStatus.IN_PROGRESS, // 게임 진행 상태
            playingTime = 120000L // 2분
        )
    }

    private fun onDragStart(startX: Float, startY: Float) {
        Log.d("asdf", "onDragStart() called with: startX = $startX, startY = $startY")
//        _gameStateFlow.update { state ->
//            state.copy(dragStart = startX to startY)
//        }
    }

    private fun onDragEnd(endX: Float, endY: Float) {
        Log.d("asdf", "onDragEnd() called with: endX = $endX, endY = $endY")
        val (startX, startY) = gameStateFlow.value.dragStart ?: return

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
                    add(Orange(row, col, values[row * 10 + col]))
                }
            }
        }
    }
}