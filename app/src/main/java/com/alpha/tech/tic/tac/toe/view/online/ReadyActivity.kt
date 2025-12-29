package com.alpha.tech.tic.tac.toe.view.online

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.alpha.tech.tic.tac.toe.base.BaseActivity
import com.alpha.tech.tic.tac.toe.databinding.ActivityReadyBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class ReadyActivity : BaseActivity() {

    private lateinit var binding: ActivityReadyBinding

    private val db = FirebaseDatabase
        .getInstance("https://tic-tac-toe-xx-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference

    private lateinit var roomId: String
    private lateinit var playerId: String
    private var playerNumber = ""
    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getStringExtra("room_id") ?: ""
        playerNumber = intent.getStringExtra("player_number") ?: "player1"
        playerId = intent.getStringExtra("player_id") ?: UUID.randomUUID().toString()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                leaveRoom()
                finish()
            }
        })

        binding.tvRoomName.text = roomId

        setupPresence()
        setupReadyButton()
        listenRoomStatus()
    }

    private fun leaveRoom() {
        val roomRef = db.child("rooms").child(roomId)
        val playerRef = roomRef.child(playerNumber)
        val readyRef = roomRef.child(if (playerNumber == "player1") "ready1" else "ready2")
        val gameStartedRef = roomRef.child("gameStarted")

        playerRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val value = currentData.value as? String ?: ""
                if (value == playerId) {
                    currentData.value = ""
                    readyRef.setValue(false)

                    if (playerNumber == "player1") {
                        gameStartedRef.setValue(false)
                    }
                }
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

    private fun setupPresence() {
        val roomRef = db.child("rooms").child(roomId)
        val playerRef = roomRef.child(playerNumber)
        val readyRef = roomRef.child(if (playerNumber == "player1") "ready1" else "ready2")

        val connectedRef = db.child(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    playerRef.setValue(playerId)
                    readyRef.onDisconnect().setValue(false)
                    playerRef.onDisconnect().removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupReadyButton() {
        binding.btnReady.setOnClickListener {
            isReady = !isReady
            val readyRef = db.child("rooms").child(roomId).child(
                if (playerNumber == "player1") "ready1" else "ready2"
            )
            readyRef.setValue(isReady)
            binding.btnReady.text = if (isReady) "Hủy sẵn sàng" else "Sẵn sàng"
        }
    }


    private fun listenRoomStatus() {
        val roomRef = db.child("rooms").child(roomId)
        roomRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ready1 = snapshot.child("ready1").getValue(Boolean::class.java) ?: false
                val ready2 = snapshot.child("ready2").getValue(Boolean::class.java) ?: false
                val gameStarted =
                    snapshot.child("gameStarted").getValue(Boolean::class.java) ?: false

                binding.tvReady1.text = if (ready1) "Sẵn sàng" else "Chưa sẵn sàng"
                binding.tvReady2.text = if (ready2) "Sẵn sàng" else "Chưa sẵn sàng"

                if (ready1 && ready2 && !gameStarted) {
                    if (playerNumber == "player1") {
                        roomRef.child("gameStarted").setValue(true)
                        roomRef.child("turn").setValue("player1")
                        roomRef.child("board").setValue(
                            mapOf(
                                "00" to "", "01" to "", "02" to "",
                                "10" to "", "11" to "", "12" to "",
                                "20" to "", "21" to "", "22" to ""
                            )
                        )
                    }
                    val intent = Intent(this@ReadyActivity, StartOnlineActivity::class.java)
                    intent.putExtra("room_id", roomId)
                    intent.putExtra("player_number", playerNumber)
                    intent.putExtra("current_player_id", playerId)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveRoom()
    }
}