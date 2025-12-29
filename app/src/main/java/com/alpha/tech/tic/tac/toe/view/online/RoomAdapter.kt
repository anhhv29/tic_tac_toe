package com.alpha.tech.tic.tac.toe.view.online

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alpha.tech.tic.tac.toe.databinding.ItemRoomBinding
import com.alpha.tech.tic.tac.toe.model.Room

class RoomAdapter(
    private val rooms: List<Room>,
    private val onClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        with(holder.binding) {
            tvRoomName.text = "Bàn ${position + 1}"
            tvPlayerCount.text = "${room.playerCount()} / 2"

            root.alpha = if (room.isFull()) 0.4f else 1f
            root.setOnClickListener {
                if (!room.isFull()) onClick(room)
            }
        }
    }

    override fun getItemCount() = rooms.size
}