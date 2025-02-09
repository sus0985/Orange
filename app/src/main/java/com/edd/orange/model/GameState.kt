package com.edd.orange.model


enum class GameStatus {
    WAITING,
    IN_PROGRESS,
    GAME_OVER
}

data class GameState(
    val oranges: List<Orange> = emptyList(),
    val score: Int = 0,
    val gameStatus: GameStatus = GameStatus.WAITING,
    val playingTime: Long = 120000L,
    val dragStart: Pair<Int, Int>? = null,
    val dragEnd: Pair<Int, Int>? = null,
)
