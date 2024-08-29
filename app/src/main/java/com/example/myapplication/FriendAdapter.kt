package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendAdapter(private val friends: MutableList<Friend>) : RecyclerView.Adapter<FriendAdapter.FriendHolder>() {

    class FriendHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        val usernameTextView : TextView = itemView.findViewById(R.id.username)
        val booksCollectedTextView : TextView = itemView.findViewById(R.id.booksCollected)
        //Add ProfilePIC
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_list_item, parent, false)
        return FriendHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        val friend = friends[position]
        holder.usernameTextView.text = friend.username
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    fun addUsername(friend: Friend) {
        friends.add(friend)
        notifyItemInserted(friends.size - 1)
    }

}