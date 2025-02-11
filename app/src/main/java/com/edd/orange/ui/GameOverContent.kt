package com.edd.orange.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameOverContent(
    uiState: GameViewModel.GameUiState,
    onClickRetry: () -> Unit
) {
    Text("Game Over! Score: ${uiState.score}")
    Spacer(modifier = Modifier.padding(16.dp))
    Button(onClick = onClickRetry) {
        Text("Retry")
    }
}