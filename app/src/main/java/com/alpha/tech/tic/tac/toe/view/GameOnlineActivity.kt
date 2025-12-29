package com.alpha.tech.tic.tac.toe.view

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import com.alpha.tech.tic.tac.toe.base.BaseActivity
import com.alpha.tech.tic.tac.toe.databinding.ActivityAfterStartBinding
import com.alpha.tech.tic.tac.toe.databinding.DialogLayoutBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class GameOnlineActivity : BaseActivity() {

    enum class Cell { EMPTY, X, O }

    private lateinit var binding: ActivityAfterStartBinding

    private val db = FirebaseDatabase.getInstance().reference
    private lateinit var roomId: String
    private lateinit var playerId: String
    private var playerNumber = ""

    private val board = Array(3) { Array(3) { Cell.EMPTY } }
    private var turn = "player1"
    private var winner: Cell = Cell.EMPTY
    private var status = "ongoing"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerId = UUID.randomUUID().toString()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        setupClicks()

        val createNewRoom = intent.getBooleanExtra("create_room", true)
        if (createNewRoom) {
            roomId = createRoom()
            playerNumber = "player1"
        } else {
            roomId = intent.getStringExtra("room_id") ?: ""
            playerNumber = "player2"
            joinRoom(roomId)
        }

        listenRoomChanges()
    }

    private fun createRoom(): String {
        val newRoomId = db.child("games").push().key!!
        val initialBoard = List(3) { List(3) { "EMPTY" } }
        val gameData = mapOf(
            "player1" to playerId,
            "player2" to "",
            "board" to initialBoard,
            "turn" to "player1",
            "winner" to "EMPTY",
            "status" to "ongoing"
        )
        db.child("games").child(newRoomId).setValue(gameData)
        return newRoomId
    }

    private fun joinRoom(roomId: String) {
        db.child("games").child(roomId).child("player2").setValue(playerId)
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
        if (board[x][y] != Cell.EMPTY || status != "ongoing" || turn != playerNumber) return

        val cell = if (playerNumber == "player1") Cell.X else Cell.O
        makeMoveOnline(x, y, cell)
    }

    private fun makeMoveOnline(x: Int, y: Int, cell: Cell) {
        val boardSnapshot = board.map { row -> row.map { it.name } }
        val mutableBoard = boardSnapshot.map { it.toMutableList() }.toMutableList()
        mutableBoard[x][y] = cell.name

        val nextTurn = if (playerNumber == "player1") "player2" else "player1"
        db.child("games").child(roomId).updateChildren(
            mapOf(
                "board" to mutableBoard,
                "turn" to nextTurn
            )
        )
    }

    private fun listenRoomChanges() {
        db.child("games").child(roomId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val boardData =
                    snapshot.child("board")
                        .getValue(object : GenericTypeIndicator<List<List<String>>>() {})!!
                for (i in 0..2) {
                    for (j in 0..2) {
                        board[i][j] = when (boardData[i][j]) {
                            "X" -> Cell.X
                            "O" -> Cell.O
                            else -> Cell.EMPTY
                        }
                    }
                }

                turn = snapshot.child("turn").value.toString()
                val winnerStr = snapshot.child("winner").value.toString()
                status = snapshot.child("status").value.toString()
                winner = when (winnerStr) {
                    "X" -> Cell.X
                    "O" -> Cell.O
                    else -> Cell.EMPTY
                }

                updateBoard()
                updateTurnText()

                if (status == "finished" && winner != Cell.EMPTY) {
                    val winnerText =
                        if ((winner == Cell.X && playerNumber == "player1") || (winner == Cell.O && playerNumber == "player2")) "You won!" else "You lost!"
                    showDialog(winnerText)
                } else if (status == "finished") {
                    showDialog("Draw!")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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
                when (board[i][j]) {
                    Cell.X -> cells[idx].setImageResource(android.R.drawable.ic_delete)
                    Cell.O -> cells[idx].setImageResource(android.R.drawable.presence_online)
                    Cell.EMPTY -> cells[idx].setImageResource(0)
                }
                idx++
            }
        }
    }

    private fun updateTurnText() {
        binding.tvTurn.text =
            if (status != "ongoing") "" else if (turn == playerNumber) "Your turn" else "Opponent's turn"
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
        dialogBinding.tvTitle.text = title
        dialog.show()

        dialogBinding.btnReset.setOnClickListener {
            dialog.dismiss()
            resetGame()
        }
        dialogBinding.btnPlayAgain.setOnClickListener {
            dialog.dismiss()
            resetGame()
        }
    }

    private fun resetGame() {
        for (i in 0..2) for (j in 0..2) board[i][j] = Cell.EMPTY
        winner = Cell.EMPTY
        status = "ongoing"
        turn = "player1"

        val resetBoard = List(3) { List(3) { "EMPTY" } }
        db.child("games").child(roomId).updateChildren(
            mapOf(
                "board" to resetBoard,
                "turn" to "player1",
                "winner" to "EMPTY",
                "status" to "ongoing"
            )
        )
        updateBoard()
        updateTurnText()
    }
}