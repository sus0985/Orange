package com.edd.orange.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun WaitingContent(onStartGame: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Orange Game")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartGame) {
            Text("Start Game")
        }
    }
}