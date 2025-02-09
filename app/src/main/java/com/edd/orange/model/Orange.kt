package com.edd.orange.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Orange(
    val row: Int,
    val col: Int,
    val value: Int
) {
    var removed by mutableStateOf(false)
}
