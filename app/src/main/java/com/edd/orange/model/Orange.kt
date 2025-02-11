package com.edd.orange.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Orange(
    val value: Int,
    var x: Float = 0f,
    var y: Float = 0f
) {
    var removed by mutableStateOf(false)
}
