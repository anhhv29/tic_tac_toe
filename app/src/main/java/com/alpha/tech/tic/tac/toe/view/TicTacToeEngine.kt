package com.alpha.tech.tic.tac.toe.view

/* =================== GAME ENGINE =================== */

class TicTacToeEngine {
    val board = Array(3) { Array(3) { AfterStartActivity.Cell.EMPTY } }
    var winner: AfterStartActivity.Cell = AfterStartActivity.Cell.EMPTY

    fun makeMove(x: Int, y: Int, player: AfterStartActivity.Cell): Boolean {
        if (board[x][y] != AfterStartActivity.Cell.EMPTY) return false
        board[x][y] = player
        winner = checkWinner(board) ?: AfterStartActivity.Cell.EMPTY
        return true
    }

    fun checkWinner(boardState: Array<Array<AfterStartActivity.Cell>> = board): AfterStartActivity.Cell? {
        for (i in 0..2) {
            if (boardState[i][0] != AfterStartActivity.Cell.EMPTY &&
                boardState[i][0] == boardState[i][1] && boardState[i][1] == boardState[i][2]
            ) return boardState[i][0]

            if (boardState[0][i] != AfterStartActivity.Cell.EMPTY &&
                boardState[0][i] == boardState[1][i] && boardState[1][i] == boardState[2][i]
            ) return boardState[0][i]
        }
        if (boardState[0][0] != AfterStartActivity.Cell.EMPTY &&
            boardState[0][0] == boardState[1][1] && boardState[1][1] == boardState[2][2]
        ) return boardState[0][0]

        if (boardState[0][2] != AfterStartActivity.Cell.EMPTY &&
            boardState[0][2] == boardState[1][1] && boardState[1][1] == boardState[2][0]
        ) return boardState[0][2]

        return null
    }

    fun isDraw(): Boolean {
        if (winner != AfterStartActivity.Cell.EMPTY) return false
        return board.all { row -> row.all { it != AfterStartActivity.Cell.EMPTY } }
    }

    fun reset() {
        for (i in 0..2) for (j in 0..2) board[i][j] = AfterStartActivity.Cell.EMPTY
        winner = AfterStartActivity.Cell.EMPTY
    }

    fun getCpuMove(
        difficulty: AfterStartActivity.Difficulty,
        cpuCell: AfterStartActivity.Cell
    ): Pair<Int, Int>? {
        val opponent =
            if (cpuCell == AfterStartActivity.Cell.X) AfterStartActivity.Cell.O else AfterStartActivity.Cell.X
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) for (j in 0..2)
            if (board[i][j] == AfterStartActivity.Cell.EMPTY) emptyCells.add(i to j)

        if (emptyCells.isEmpty()) return null

        return when (difficulty) {
            AfterStartActivity.Difficulty.EASY -> emptyCells.random()

            AfterStartActivity.Difficulty.MEDIUM -> {
                for ((i, j) in emptyCells) {
                    board[i][j] = opponent
                    if (checkWinner(board) == opponent) {
                        board[i][j] = AfterStartActivity.Cell.EMPTY
                        return i to j
                    }
                    board[i][j] = AfterStartActivity.Cell.EMPTY
                }
                emptyCells.random()
            }

            AfterStartActivity.Difficulty.HARD -> {
                for ((i, j) in emptyCells) {
                    board[i][j] = cpuCell
                    if (checkWinner(board) == cpuCell) {
                        board[i][j] = AfterStartActivity.Cell.EMPTY
                        return i to j
                    }
                    board[i][j] = AfterStartActivity.Cell.EMPTY
                }
                for ((i, j) in emptyCells) {
                    board[i][j] = opponent
                    if (checkWinner(board) == opponent) {
                        board[i][j] = AfterStartActivity.Cell.EMPTY
                        return i to j
                    }
                    board[i][j] = AfterStartActivity.Cell.EMPTY
                }
                if (board[1][1] == AfterStartActivity.Cell.EMPTY) return 1 to 1
                emptyCells.random()
            }

            AfterStartActivity.Difficulty.IMPOSSIBLE -> {
                var bestScore = Int.MIN_VALUE
                var bestMove: Pair<Int, Int>? = null
                for ((i, j) in emptyCells) {
                    board[i][j] = cpuCell
                    val score = minimax(false, cpuCell, opponent)
                    board[i][j] = AfterStartActivity.Cell.EMPTY
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = i to j
                    }
                }
                bestMove
            }
        }
    }

    private fun minimax(
        isMaximizing: Boolean,
        cpuCell: AfterStartActivity.Cell,
        opponent: AfterStartActivity.Cell
    ): Int {
        val winner = checkWinner()
        if (winner == cpuCell) return 10
        if (winner == opponent) return -10
        if (isDraw()) return 0

        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) for (j in 0..2)
            if (board[i][j] == AfterStartActivity.Cell.EMPTY) emptyCells.add(i to j)

        return if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for ((i, j) in emptyCells) {
                board[i][j] = cpuCell
                val score = minimax(false, cpuCell, opponent)
                board[i][j] = AfterStartActivity.Cell.EMPTY
                bestScore = maxOf(bestScore, score)
            }
            bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for ((i, j) in emptyCells) {
                board[i][j] = opponent
                val score = minimax(true, cpuCell, opponent)
                board[i][j] = AfterStartActivity.Cell.EMPTY
                bestScore = minOf(bestScore, score)
            }
            bestScore
        }
    }
}