package com.alpha.tech.tic.tac.toe.view.online

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.alpha.tech.tic.tac.toe.R
import com.alpha.tech.tic.tac.toe.base.BaseActivity
import com.alpha.tech.tic.tac.toe.databinding.ActivityAfterStartBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class StartOnlineActivity : BaseActivity() {

    private lateinit var binding: ActivityAfterStartBinding

    private lateinit var roomId: String
    private lateinit var playerNumber: String
    private var player1ax = true

    private val db = FirebaseDatabase
        .getInstance("https://tic-tac-toe-xx-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference

    private lateinit var cells: Array<Array<ImageView>>
    private var board = Array(3) { Array(3) { "" } }

    private var isMyTurn = false
    private var gameEnded = false
    private var isLeaving = false

    private var player1Score = 0
    private var player2Score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getStringExtra("room_id") ?: return
        playerNumber = intent.getStringExtra("player_number") ?: "player1"
        player1ax = intent.getBooleanExtra("player1ax", true)

        setupPlayerNames()
        setupBackPress()
        setupPresence()
        setupBoard()
        listenBoard()
        listenTurn()
        listenOpponentLeave()
    }

    private fun setupPlayerNames() {
        if (playerNumber == "player1") {
            binding.tvPlayerOne.text = "You"
            binding.tvPlayerTwo.text = "Opponent"
        } else {
            binding.tvPlayerOne.text = "Opponent"
            binding.tvPlayerTwo.text = "You"
        }

        binding.tvPlayerOne.setTextColor(Color.BLUE)
        binding.tvPlayerTwo.setTextColor(Color.RED)
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                leaveGame()
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveGame()
    }

    private fun leaveGame() {
        isLeaving = true
        db.child("rooms").child(roomId).child(playerNumber).removeValue()
    }

    private fun setupPresence() {
        db.child("rooms")
            .child(roomId)
            .child(playerNumber)
            .onDisconnect()
            .removeValue()
    }

    private fun setupBoard() {
        cells = arrayOf(
            arrayOf(binding.ivZz, binding.ivZo, binding.ivZt),
            arrayOf(binding.ivOz, binding.ivOo, binding.ivOt),
            arrayOf(binding.ivTz, binding.ivTo, binding.ivTt)
        )

        for (i in 0..2) {
            for (j in 0..2) {
                cells[i][j].setOnClickListener {
                    if (!gameEnded && isMyTurn && board[i][j].isEmpty()) {
                        makeMove(i, j)
                    }
                }
            }
        }
    }

    private fun makeMove(row: Int, col: Int) {
        val roomRef = db.child("rooms").child(roomId)

        roomRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val turn = currentData.child("turn").getValue(String::class.java) ?: "player1"
                if (turn != playerNumber) return Transaction.abort()

                val key = "$row$col"
                if (!currentData.child("board").child(key).value.toString().isEmpty())
                    return Transaction.abort()

                currentData.child("board").child(key).value = playerNumber
                currentData.child("turn").value =
                    if (playerNumber == "player1") "player2" else "player1"

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
            }
        })
    }

    private fun listenBoard() {
        db.child("rooms").child(roomId).child("board")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in 0..2) {
                        for (j in 0..2) {
                            val value = snapshot.child("$i$j").getValue(String::class.java) ?: ""
                            board[i][j] = value
                            updateBoardUI(i, j, value)
                        }
                    }
                    checkGameResult()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun listenTurn() {
        db.child("rooms").child(roomId).child("turn")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val turn = snapshot.getValue(String::class.java) ?: "player1"
                    isMyTurn = turn == playerNumber
                    binding.tvTurn.text =
                        if (isMyTurn) "Your turn" else "Opponent's turn"
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun listenOpponentLeave() {
        val opponent = if (playerNumber == "player1") "player2" else "player1"

        db.child("rooms").child(roomId).child(opponent)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists() && !gameEnded && !isLeaving) {
                        gameEnded = true
                        Toast.makeText(
                            this@StartOnlineActivity,
                            "Opponent left. You win!",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun checkGameResult() {
        val winner = getWinner()
        if (winner.isNotEmpty()) {
            gameEnded = true
            Toast.makeText(this, "$winner wins!", Toast.LENGTH_LONG).show()

            if (winner == "player1") player1Score++ else player2Score++
            binding.tvPlayer1Score.text = player1Score.toString()
            binding.tvPlayer2Score.text = player2Score.toString()

            resetBoard()
        } else if (isDraw()) {
            gameEnded = true
            Toast.makeText(this, "Draw!", Toast.LENGTH_LONG).show()
            resetBoard()
        }
    }

    private fun getWinner(): String {
        for (i in 0..2) {
            if (board[i][0].isNotEmpty() &&
                board[i][0] == board[i][1] &&
                board[i][1] == board[i][2]
            ) return board[i][0]

            if (board[0][i].isNotEmpty() &&
                board[0][i] == board[1][i] &&
                board[1][i] == board[2][i]
            ) return board[0][i]
        }

        if (board[0][0].isNotEmpty() &&
            board[0][0] == board[1][1] &&
            board[1][1] == board[2][2]
        ) return board[0][0]

        if (board[0][2].isNotEmpty() &&
            board[0][2] == board[1][1] &&
            board[1][1] == board[2][0]
        ) return board[0][2]

        return ""
    }

    private fun isDraw(): Boolean {
        return board.all { row -> row.all { it.isNotEmpty() } }
    }

    private fun resetBoard() {
        val roomRef = db.child("rooms").child(roomId)

        roomRef.child("board").setValue(
            mapOf(
                "00" to "", "01" to "", "02" to "",
                "10" to "", "11" to "", "12" to "",
                "20" to "", "21" to "", "22" to ""
            )
        )

        roomRef.child("turn").setValue("player1")
        gameEnded = false
    }

    private fun updateBoardUI(row: Int, col: Int, value: String) {
        val cell = cells[row][col]
        when (value) {
            "player1" ->
                cell.setImageResource(
                    if (player1ax) R.drawable.ic_tic_tac_toe_x
                    else R.drawable.ic_tic_tac_toe_o
                )

            "player2" ->
                cell.setImageResource(
                    if (player1ax) R.drawable.ic_tic_tac_toe_o
                    else R.drawable.ic_tic_tac_toe_x
                )

            else -> cell.setImageDrawable(null)
        }
    }
}