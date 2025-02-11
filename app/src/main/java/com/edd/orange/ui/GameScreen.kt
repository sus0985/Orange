package com.edd.orange.ui

import android.graphics.Paint
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edd.orange.model.GameState
import com.edd.orange.model.GameStatus
import com.edd.orange.model.Orange
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameStateFlow.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (gameState.gameStatus) {
            GameStatus.WAITING -> {
                WaitingScreen(viewModel)
            }

            GameStatus.IN_PROGRESS -> {
                PlayingScreen(viewModel, gameState)
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
fun WaitingScreen(viewModel: GameViewModel) {
    Text("Game is waiting. Press start to begin!")
    Button(onClick = { viewModel.handleIntent(GameViewModel.GameIntent.StartGame) }) {
        Text("Start Game")
    }
}

@Composable
fun PlayingScreen(
    viewModel: GameViewModel,
    gameState: GameState
) {
    val remainingTime by viewModel.remainingTime.collectAsStateWithLifecycle()
    val isDragging by viewModel.isDragging.collectAsStateWithLifecycle()
    val contained = remember { mutableSetOf<Orange>() }

    var startX by remember { mutableIntStateOf(0) }
    var startY by remember { mutableIntStateOf(0) }
    var currentX by remember { mutableIntStateOf(0) }
    var currentY by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            viewModel.updateRemainingTime(remainingTime - 1000)
            delay(1000)
        }

        viewModel.handleIntent(GameViewModel.GameIntent.EndGame(score))
    }

    RemainingTime(timeRemaining = remainingTime, score)
    BoxWithConstraints(
        modifier = Modifier
            .padding(16.dp)
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
                                viewModel.setDragging(true)
                            }

                            change.changedToUp() -> {
                                if (contained.sumOf { it.value } == 10) {
                                    contained.forEach {
                                        it.removed = true
                                    }
                                    score += contained.size
                                }
                                contained.clear()
                                viewModel.setDragging(false)
                            }

                            else -> {
                                currentX = change.position.x.roundToInt()
                                currentY = change.position.y.roundToInt()
                            }
                        }
                    }
                }
                return@pointerInput
            }
    ) {
        val left = minOf(startX, currentX).toFloat()
        val top = minOf(startY, currentY).toFloat()
        val width = abs(currentX - startX).dp
        val height = abs(currentY - startY).dp

        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val gridColumns = 10
            val spacing = 4.dp.toPx()
            val cellSize = (size.width - (gridColumns + 1) * spacing) / gridColumns

            gameState.oranges.forEachIndexed { index, orange ->
                if (orange.removed) return@forEachIndexed

                val row = index / gridColumns
                val col = index % gridColumns

                val x = spacing + (cellSize + spacing) * col + cellSize / 2
                val y = spacing + (cellSize + spacing) * row + cellSize / 2

                orange.x = x
                orange.y = y

                var color = Color(0xFFFFA500)
                if (isDragging) {
                    val right = left + width.value
                    val bottom = top + height.value

                    val radius = cellSize / 4

                    val isInsideRectangle =
                        (x - radius >= left) && (x + radius <= right) && (y - radius >= top) && (y + radius <= bottom)

                    if (isInsideRectangle) {
                        color = Color(0x33FFA500)
                        contained.add(orange)
                    } else {
                        contained.remove(orange)
                    }
                }

                drawCircle(
                    color = color,
                    radius = cellSize / 2,
                    center = Offset(x, y)
                )

                val orangeValue = orange.value.toString()
                val textPaint = TextPaint().apply {
                    this.color = Color.White.toArgb()
                    textAlign = Paint.Align.CENTER
                    textSize = 14.sp.toPx()
                }

                drawContext.canvas.nativeCanvas.drawText(
                    orangeValue,
                    x,
                    y + textPaint.textSize / 3,
                    textPaint
                )

            }
        }


        if (isDragging) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color(0x55FFFFFF),
                    topLeft = Offset(left, top),
                    size = Size(width.value, height.value)
                )
            }
        }
    }
}

@Composable
fun RemainingTime(timeRemaining: Long, point: Int) {
    Column {
        Text("Time Remaining: ${timeRemaining / 1000} seconds")
        Text("Point: $point")
    }
}

@Composable
@Preview
fun GameScreenPreview() {
    GameScreen()
}