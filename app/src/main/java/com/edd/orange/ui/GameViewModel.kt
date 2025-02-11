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
        data class EndGame(val score: Int) : GameIntent()
    }


    private val _gameStateFlow = MutableStateFlow(GameState())
    val gameStateFlow = _gameStateFlow.asStateFlow()

    private val _isDragging = MutableStateFlow(false)
    val isDragging = _isDragging.asStateFlow()

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime = _remainingTime.asStateFlow()


    fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.StartGame -> startGame()
            is GameIntent.EndGame -> {
                _gameStateFlow.value = _gameStateFlow.value.copy(
                    gameStatus = GameStatus.GAME_OVER,
                    score = intent.score
                )
            }
        }
    }

    fun setDragging(isDragging: Boolean) {
        _isDragging.value = isDragging
    }

    fun updateRemainingTime(time: Long) {
        _remainingTime.value = time
    }


    private fun startGame() {
        _gameStateFlow.value = GameState(
            oranges = generateOranges(),
            gameStatus = GameStatus.IN_PROGRESS,
        )

        _remainingTime.value = 120000L
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
}