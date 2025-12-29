package com.alpha.tech.tic.tac.toe.model

data class Room(
    val id: String = "",
    val player1: String = "",
    val player2: String = "",
    val ready1: Boolean = false,
    val ready2: Boolean = false,
    val status: String = "waiting"
) {
    fun playerCount(): Int {
        var count = 0
        if (player1.isNotEmpty()) count++
        if (player2.isNotEmpty()) count++
        return count
    }

    fun isFull() = playerCount() == 2

}
