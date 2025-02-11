package com.edd.orange.ui

import android.graphics.Paint
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val GRID_COLUMN = 10


@Composable
fun GamePlayContent(
    uiState: GameViewModel.GameUiState,
    onDragEvent: (DragEvent) -> Unit
) {
    val density = LocalDensity.current
    Column(modifier = Modifier.fillMaxSize()) {
        ScoreAndTime(uiState)
        OrangeGameCanvas(uiState, density, onDragEvent)
    }
}

@Composable
fun ScoreAndTime(uiState: GameViewModel.GameUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Time Remaining: ${uiState.remainingTime / 1000} seconds")
        Text("Score: ${uiState.score}")
    }
}

@Composable
fun OrangeGameCanvas(
    uiState: GameViewModel.GameUiState,
    density: Density,
    onDragEvent: (DragEvent) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> onDragEvent(DragEvent.Start(offset)) },
                    onDrag = { change, _ -> onDragEvent(DragEvent.Update(change.position)) },
                    onDragEnd = { onDragEvent(DragEvent.End) }
                )
            }
    ) {
        val gridColumns = GRID_COLUMN
        val spacingPx = with(density) { 4.dp.toPx() }
        val canvasWidthPx = with(density) { maxWidth.toPx() }
        val cellSize = (canvasWidthPx - (gridColumns + 1) * spacingPx) / gridColumns

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOranges(uiState, gridColumns, spacingPx, cellSize, density)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDragRect(uiState)
        }
    }
}


private fun DrawScope.drawOranges(
    uiState: GameViewModel.GameUiState,
    gridColumns: Int,
    spacingPx: Float,
    cellSize: Float,
    density: Density
) {
    uiState.oranges.forEachIndexed { index, orange ->
        if (!orange.removed) {
            val row = index / gridColumns
            val col = index % gridColumns

            val centerX = spacingPx + (cellSize + spacingPx) * col + cellSize / 2
            val centerY = spacingPx + (cellSize + spacingPx) * row + cellSize / 2

            orange.x = centerX
            orange.y = centerY

            val circleColor = if (uiState.isDragging &&
                uiState.dragStart != null && uiState.currentDrag != null &&
                centerX in minOf(uiState.dragStart.first, uiState.currentDrag.first)..maxOf(uiState.dragStart.first, uiState.currentDrag.first) &&
                centerY in minOf(uiState.dragStart.second, uiState.currentDrag.second)..maxOf(uiState.dragStart.second, uiState.currentDrag.second)
            ) {
                Color(0x66FFA500)
            } else {
                Color(0xFFFFA500)
            }

            drawCircle(
                color = circleColor,
                radius = cellSize / 2,
                center = Offset(centerX, centerY)
            )

            val textPaint = TextPaint().apply {
                color = Color.White.toArgb()
                textAlign = Paint.Align.CENTER
                textSize = with(density) { 14.sp.toPx() }
            }
            drawContext.canvas.nativeCanvas.drawText(
                orange.value.toString(),
                centerX,
                centerY + textPaint.textSize / 3,
                textPaint
            )
        }
    }
}

private fun DrawScope.drawDragRect(
    uiState: GameViewModel.GameUiState,
) {
    if (uiState.isDragging && uiState.dragStart != null && uiState.currentDrag != null) {
        val left = minOf(uiState.dragStart.first, uiState.currentDrag.first)
        val top = minOf(uiState.dragStart.second, uiState.currentDrag.second)
        val right = maxOf(uiState.dragStart.first, uiState.currentDrag.first)
        val bottom = maxOf(uiState.dragStart.second, uiState.currentDrag.second)
        drawRect(
            color = Color(0x55FFFFFF),
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top)
        )
    }
}