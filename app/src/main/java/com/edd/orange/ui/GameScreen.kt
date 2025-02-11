package com.edd.orange.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edd.orange.model.GameStatus


sealed class DragEvent {
    data class Start(val position: Offset) : DragEvent()
    data class Update(val position: Offset) : DragEvent()
    data object End : DragEvent()
}


@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState.gameStatus) {
            GameStatus.WAITING -> {
                WaitingContent(
                    onStartGame = { viewModel.handleIntent(GameViewModel.GameIntent.StartGame) }
                )
            }

            GameStatus.IN_PROGRESS -> {
                GamePlayContent(
                    uiState = uiState,
                    onDragEvent = { event ->
                        when (event) {
                            is DragEvent.Start ->
                                viewModel.handleIntent(
                                    GameViewModel.GameIntent.StartDrag(
                                        event.position.x,
                                        event.position.y
                                    )
                                )

                            is DragEvent.Update ->
                                viewModel.handleIntent(
                                    GameViewModel.GameIntent.UpdateDrag(
                                        event.position.x,
                                        event.position.y
                                    )
                                )

                            is DragEvent.End -> viewModel.handleIntent(GameViewModel.GameIntent.EndDrag)
                        }
                    }
                )
            }

            GameStatus.GAME_OVER -> {
                GameOverContent(
                    uiState = uiState,
                    onClickRetry = { viewModel.handleIntent(GameViewModel.GameIntent.StartGame) }
                )
            }
        }
    }
}


@Composable
@Preview
fun GameScreenPreview() {
    GameScreen()
}