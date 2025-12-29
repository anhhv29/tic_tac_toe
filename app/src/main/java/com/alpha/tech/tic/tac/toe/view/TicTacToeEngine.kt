package com.alpha.tech.tic.tac.toe.view

/* =================== GAME ENGINE =================== */

class TicTacToeEngine {
    val board = Array(3) { Array(3) { StartOfflineActivity.Cell.EMPTY } }
    var winner: StartOfflineActivity.Cell = StartOfflineActivity.Cell.EMPTY

    fun makeMove(x: Int, y: Int, player: StartOfflineActivity.Cell): Boolean {
        if (board[x][y] != StartOfflineActivity.Cell.EMPTY) return false
        board[x][y] = player
        winner = checkWinner(board) ?: StartOfflineActivity.Cell.EMPTY
        return true
    }

    fun checkWinner(boardState: Array<Array<StartOfflineActivity.Cell>> = board): StartOfflineActivity.Cell? {
        for (i in 0..2) {
            if (boardState[i][0] != StartOfflineActivity.Cell.EMPTY &&
                boardState[i][0] == boardState[i][1] && boardState[i][1] == boardState[i][2]
            ) return boardState[i][0]

            if (boardState[0][i] != StartOfflineActivity.Cell.EMPTY &&
                boardState[0][i] == boardState[1][i] && boardState[1][i] == boardState[2][i]
            ) return boardState[0][i]
        }
        if (boardState[0][0] != StartOfflineActivity.Cell.EMPTY &&
            boardState[0][0] == boardState[1][1] && boardState[1][1] == boardState[2][2]
        ) return boardState[0][0]

        if (boardState[0][2] != StartOfflineActivity.Cell.EMPTY &&
            boardState[0][2] == boardState[1][1] && boardState[1][1] == boardState[2][0]
        ) return boardState[0][2]

        return null
    }

    fun isDraw(): Boolean {
        if (winner != StartOfflineActivity.Cell.EMPTY) return false
        return board.all { row -> row.all { it != StartOfflineActivity.Cell.EMPTY } }
    }

    fun reset() {
        for (i in 0..2) for (j in 0..2) board[i][j] = StartOfflineActivity.Cell.EMPTY
        winner = StartOfflineActivity.Cell.EMPTY
    }

    fun getCpuMove(
        difficulty: StartOfflineActivity.Difficulty,
        cpuCell: StartOfflineActivity.Cell
    ): Pair<Int, Int>? {
        val opponent =
            if (cpuCell == StartOfflineActivity.Cell.X) StartOfflineActivity.Cell.O else StartOfflineActivity.Cell.X
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) for (j in 0..2)
            if (board[i][j] == StartOfflineActivity.Cell.EMPTY) emptyCells.add(i to j)

        if (emptyCells.isEmpty()) return null

        return when (difficulty) {
            StartOfflineActivity.Difficulty.EASY -> emptyCells.random()

            StartOfflineActivity.Difficulty.MEDIUM -> {
                for ((i, j) in emptyCells) {
                    board[i][j] = opponent
                    if (checkWinner(board) == opponent) {
                        board[i][j] = StartOfflineActivity.Cell.EMPTY
                        return i to j
                    }
                    board[i][j] = StartOfflineActivity.Cell.EMPTY
                }
                emptyCells.random()
            }

            StartOfflineActivity.Difficulty.HARD -> {
                for ((i, j) in emptyCells) {
                    board[i][j] = cpuCell
                    if (checkWinner(board) == cpuCell) {
                        board[i][j] = StartOfflineActivity.Cell.EMPTY
                        return i to j
                    }
                    board[i][j] = StartOfflineActivity.Cell.EMPTY
                }
                for ((i, j) in emptyCells) {
                    board[i][j] = opponent
                    if (checkWinner(board) == opponent) {
                        board[i][j] = StartOfflineActivity.Cell.EMPTY
                        return i to j
                    }
                    board[i][j] = StartOfflineActivity.Cell.EMPTY
                }
                if (board[1][1] == StartOfflineActivity.Cell.EMPTY) return 1 to 1
                emptyCells.random()
            }

            StartOfflineActivity.Difficulty.IMPOSSIBLE -> {
                var bestScore = Int.MIN_VALUE
                var bestMove: Pair<Int, Int>? = null
                for ((i, j) in emptyCells) {
                    board[i][j] = cpuCell
                    val score = minimax(false, cpuCell, opponent)
                    board[i][j] = StartOfflineActivity.Cell.EMPTY
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
        cpuCell: StartOfflineActivity.Cell,
        opponent: StartOfflineActivity.Cell
    ): Int {
        val winner = checkWinner()
        if (winner == cpuCell) return 10
        if (winner == opponent) return -10
        if (isDraw()) return 0

        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) for (j in 0..2)
            if (board[i][j] == StartOfflineActivity.Cell.EMPTY) emptyCells.add(i to j)

        return if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for ((i, j) in emptyCells) {
                board[i][j] = cpuCell
                val score = minimax(false, cpuCell, opponent)
                board[i][j] = StartOfflineActivity.Cell.EMPTY
                bestScore = maxOf(bestScore, score)
            }
            bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for ((i, j) in emptyCells) {
                board[i][j] = opponent
                val score = minimax(true, cpuCell, opponent)
                board[i][j] = StartOfflineActivity.Cell.EMPTY
                bestScore = minOf(bestScore, score)
            }
            bestScore
        }
    }
}