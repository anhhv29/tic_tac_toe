package com.alpha.tech.tic.tac.toe.view.online

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import com.alpha.tech.tic.tac.toe.base.BaseActivity
import com.alpha.tech.tic.tac.toe.databinding.ActivityRoomListBinding
import com.alpha.tech.tic.tac.toe.model.Room
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class RoomListActivity : BaseActivity() {

    private lateinit var binding: ActivityRoomListBinding

    private val db = FirebaseDatabase
        .getInstance("https://tic-tac-toe-xx-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference
    private val rooms = mutableListOf<Room>()
    private lateinit var adapter: RoomAdapter
    private val playerId = UUID.randomUUID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        adapter = RoomAdapter(rooms) { room ->
            joinRoom(room)
        }

        binding.rvRooms.layoutManager = GridLayoutManager(this, 2)
        binding.rvRooms.adapter = adapter

        listenRooms()

    }

    private fun listenRooms() {
        db.child("rooms").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                rooms.clear()
                snapshot.children.forEach {
                    val room = it.key?.let { id ->
                        it.getValue(Room::class.java)?.copy(id = id)
                    }
                    room?.let { element ->
                        rooms.add(element)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun joinRoom(room: Room) {
        val roomRef = db.child("rooms").child(room.id)

        roomRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val p1 = currentData.child("player1").value as? String ?: ""
                val p2 = currentData.child("player2").value as? String ?: ""

                when {
                    p1.isEmpty() -> currentData.child("player1").value = playerId
                    p2.isEmpty() -> currentData.child("player2").value = playerId
                    else -> return Transaction.abort()
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (committed && snapshot != null) {
                    val updatedP1 = snapshot.child("player1").value as? String ?: ""
                    val playerNumber = if (updatedP1 == playerId) "player1" else "player2"

                    val intent = Intent(this@RoomListActivity, ReadyActivity::class.java)
                    intent.putExtra("room_id", room.id)
                    intent.putExtra("player_number", playerNumber)
                    intent.putExtra("player_id", playerId)
                    startActivity(intent)
                }
            }
        })
    }
}