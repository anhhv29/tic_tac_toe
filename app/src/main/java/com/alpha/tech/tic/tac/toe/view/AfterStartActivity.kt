package com.alpha.tech.tic.tac.toe.view

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.alpha.tech.tic.tac.toe.base.BaseActivity
import com.alpha.tech.tic.tac.toe.databinding.ActivityAfterStartBinding
import com.alpha.tech.tic.tac.toe.databinding.DialogLayoutBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AfterStartActivity : BaseActivity() {

    enum class GameMode { PVP, PVE }
    enum class Cell { EMPTY, X, O }
    enum class Difficulty { EASY, MEDIUM, HARD, IMPOSSIBLE }

    private lateinit var binding: ActivityAfterStartBinding
    private lateinit var gameMode: GameMode
    private var difficulty: Difficulty = Difficulty.EASY
    private lateinit var player1: CharSequence
    private lateinit var player2: CharSequence
    private var player1Turn = true
    private var gameNumber = 1
    private var score1 = 0
    private var score2 = 0
    private var isCpuThinking = false

    private lateinit var player1Cell: Cell
    private lateinit var player2Cell: Cell

    private val engine = TicTacToeEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        setupIntent()
        setupPlayers()
        setupClicks()
        updateGameNumber()
        updateTurnText()

        if (gameMode == GameMode.PVE && !player1Turn) {
            handleCpuMove()
        }
    }

    private fun setupIntent() {
        intent.apply {
            val players = getCharSequenceArrayExtra("players_names")
            player1 = players?.get(0) ?: "Player 1"
            player2 = players?.get(1) ?: "Player 2"

            player1Turn = getBooleanExtra("player1ax", true)

            player1Cell = if (player1Turn) Cell.X else Cell.O
            player2Cell = if (player1Turn) Cell.O else Cell.X

            gameMode =
                if (getBooleanExtra("selected_single_player", true)) GameMode.PVE else GameMode.PVP

            difficulty = when {
                getBooleanExtra("easy", false) -> Difficulty.EASY
                getBooleanExtra("medium", false) -> Difficulty.MEDIUM
                getBooleanExtra("hard", false) -> Difficulty.HARD
                getBooleanExtra("impossible", false) -> Difficulty.IMPOSSIBLE
                else -> Difficulty.EASY
            }
        }
    }

    private fun setupPlayers() {
        binding.tvPlayerOne.text = player1
        binding.tvPlayerTwo.text = player2
    }

    private fun setupClicks() = with(binding) {
        ivZz.setOnClickListener { onCellClick(0, 0) }
        ivZo.setOnClickListener { onCellClick(0, 1) }
        ivZt.setOnClickListener { onCellClick(0, 2) }
        ivOz.setOnClickListener { onCellClick(1, 0) }
        ivOo.setOnClickListener { onCellClick(1, 1) }
        ivOt.setOnClickListener { onCellClick(1, 2) }
        ivTz.setOnClickListener { onCellClick(2, 0) }
        ivTo.setOnClickListener { onCellClick(2, 1) }
        ivTt.setOnClickListener { onCellClick(2, 2) }
    }

    private fun onCellClick(x: Int, y: Int) {
        if (engine.board[x][y] != Cell.EMPTY || isCpuThinking || engine.winner != Cell.EMPTY) return

        val current = if (player1Turn) player1Cell else player2Cell
        engine.makeMove(x, y, current)
        updateBoard()
        checkGameStatus()

        player1Turn = !player1Turn
        updateTurnText()

        if (gameMode == GameMode.PVE && !player1Turn && engine.winner == Cell.EMPTY) {
            handleCpuMove()
        }
    }

    private fun handleCpuMove() {
        isCpuThinking = true
        val cpuCell = player2Cell
        lifecycleScope.launch {
            delay(500L)
            val move = engine.getCpuMove(difficulty, cpuCell)
            if (move != null) {
                engine.makeMove(move.first, move.second, cpuCell)
                updateBoard()
                checkGameStatus()
            }
            isCpuThinking = false
            player1Turn = true
            updateTurnText()
        }
    }

    private fun updateBoard() = with(binding) {
        val cells = listOf(
            ivZz, ivZo, ivZt,
            ivOz, ivOo, ivOt,
            ivTz, ivTo, ivTt
        )
        var idx = 0
        for (i in 0..2) {
            for (j in 0..2) {
                when (engine.board[i][j]) {
                    Cell.X -> cells[idx].setImageResource(android.R.drawable.ic_delete)
                    Cell.O -> cells[idx].setImageResource(android.R.drawable.presence_online)
                    Cell.EMPTY -> cells[idx].setImageResource(0)
                }
                idx++
            }
        }
    }

    private fun checkGameStatus() {
        val winnerCell = engine.checkWinner()
        if (winnerCell != null) {
            when (winnerCell) {
                player1Cell -> {
                    score1++
                    showDialog("$player1 won!")
                }

                player2Cell -> {
                    score2++
                    showDialog("$player2 won!")
                }

                else -> {}
            }
            updateScores()
        } else if (engine.isDraw()) {
            showDialog("Draw!")
        }
    }

    private fun updateTurnText() {
        val turn = "${if (player1Turn) player1 else player2}'s turn"
        binding.tvTurn.text = turn
    }

    private fun updateScores() {
        binding.tvPlayer1Score.text = score1.toString()
        binding.tvPlayer2Score.text = score2.toString()
    }

    private fun updateGameNumber() {
        binding.tvGameNumber.text = "$gameNumber"
    }

    private fun showDialog(title: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.show()

        dialogBinding.tvTitle.text = title

        gameNumber++
        updateGameNumber()

        dialogBinding.btnReset.setOnClickListener {
            dialog.dismiss()
            resetGame(false)
        }

        dialogBinding.btnPlayAgain.setOnClickListener {
            dialog.dismiss()
            resetGame(true)
        }
    }

    private fun resetGame(keepScore: Boolean = true) {
        engine.reset()
        player1Turn = (player1Cell == Cell.X)
        if (!keepScore) {
            score1 = 0
            score2 = 0
            gameNumber = 1
        }
        updateBoard()
        updateScores()
        updateGameNumber()
        updateTurnText()

        if (gameMode == GameMode.PVE && !player1Turn) {
            handleCpuMove()
        }
    }
}
