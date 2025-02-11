package com.edd.orange.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edd.orange.model.GameStatus
import com.edd.orange.model.Orange
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameStateFlow.collectAsStateWithLifecycle()

    val remainingTime by remember { mutableLongStateOf(gameState.playingTime) }
    var startX by remember { mutableIntStateOf(0) }
    var startY by remember { mutableIntStateOf(0) }
    var currentX by remember { mutableIntStateOf(0) }
    var currentY by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (gameState.gameStatus) {
            GameStatus.WAITING -> {
                // 게임 대기 상태 화면
                Text("Game is waiting. Press start to begin!")
                Button(onClick = { viewModel.handleIntent(GameViewModel.GameIntent.StartGame) }) {
                    Text("Start Game")
                }
            }

            GameStatus.IN_PROGRESS -> {
                RemainingTime(timeRemaining = remainingTime)
                Box(modifier = Modifier
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.first()

                                when {
                                    change.changedToDown() -> {
                                        startX = change.position.x.roundToInt()
                                        startY = change.position.y.roundToInt()
                                        currentX = change.position.x.roundToInt()
                                        currentY = change.position.y.roundToInt()
                                        isDragging = true
                                    }
                                    change.changedToUp() -> {
                                        isDragging = false
                                        viewModel.handleIntent(GameViewModel.GameIntent.OnDragEnd(
                                            change.position.x,
                                            change.position.y
                                        ))
                                    }
                                    else -> {
                                        currentX = change.position.x.roundToInt()
                                        currentY = change.position.y.roundToInt()
                                    }
                                }
                            }
                        }
                    }
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(10),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)

                    ) {
                        items(gameState.oranges) { orange ->
                            OrangeCell(orange = orange)
                        }
                    }

                    if (isDragging) {
                        val left = minOf(startX, currentX)
                        val top = minOf(startY, currentY)
                        val width = abs(currentX - startX).dp
                        val height = abs(currentY - startY).dp
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                color = Color(0x5500A500),
                                topLeft = IntOffset(left, top).toOffset(),
                                size = Size(width.value, height.value)
                            )
                        }
                    }
                }
            }

            GameStatus.GAME_OVER -> {
                Text("Game Over! Score: ${gameState.score}")
                Button(onClick = { viewModel.handleIntent(GameViewModel.GameIntent.StartGame) }) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun RemainingTime(timeRemaining: Long) {
    Text("Time Remaining: ${timeRemaining / 1000} seconds")
}

@Composable
fun OrangeCell(orange: Orange) {
    BoxWithConstraints(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color(0xFFFFA500), shape = CircleShape) // 오렌지 색상과 원형 모양
            .padding(5.dp)
    ) {

        orange.x = maxWidth.value.roundToInt() / 2
        orange.y = maxHeight.value.roundToInt() / 2

        Text(
            text = orange.value.toString(),
            modifier = Modifier.align(Alignment.Center),
            color = Color.Black,
            fontSize = 14.sp
        )

    }
}

@Composable
@Preview
fun GameScreenPreview() {
    GameScreen()
}