package com.example.myapplication


import android.graphics.Canvas
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class FriendListAdapter(public val friendList: MutableList<FriendInfo>, private val onFriendClickListener: OnFriendClickListener): RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>() {

    private val db = Firebase.firestore

    private val auth = Firebase.auth

    private var onItemDeletedListener: OnItemDeletedListener? = null

    fun addFriend(friend: FriendInfo) {
        friendList.add(friend)
        notifyItemInserted(friendList.size - 1)
    }

    fun saveFriend(friendInfo: FriendInfo, friendUserID: String) {
        val friend = hashMapOf(
            "username" to friendInfo.username,
            "booksCollected" to friendInfo.booksCollected,
            "profilePicture" to friendInfo.profilePictureID
        )

        var userID = auth.currentUser?.uid ?: return

        db.collection("users").document(userID).collection("friends").document(friendUserID)
            .set(friend)
            .addOnSuccessListener {
                Log.d("FriendListAdapter", "Friend saved successfully")
            }
            .addOnFailureListener {
                Log.w("FriendListAdapter", "Error saving Friend", it)
            }
    }

    fun loadFriends() {
        friendList.clear()
        val userID = auth.currentUser?.uid ?: return
        db.collection("users").document(userID).collection("friends").get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val friendId = document.id


                    db.collection("users").document(friendId).get()
                        .addOnSuccessListener { userDocument ->

                            db.collection("users").document(friendId).collection("collectedBooks")
                                .get()
                                .addOnSuccessListener { booksSnapshot ->
                                    val booksCollected = booksSnapshot.size().toString()
                                    val friendInfo = FriendInfo(
                                        userID = userDocument.getString("userID"),
                                        username = userDocument.getString("username"),
                                        booksCollected = booksCollected, // Now available
                                        profilePictureID = userDocument.getLong("profilePictureID")
                                            ?.toInt()
                                    )
                                    Log.w("FriendListAdapter", "Friend info: $friendInfo")
                                    friendList.add(friendInfo)
                                    notifyDataSetChanged()
                                }
                        }
                }
            }
    }

    fun deleteFriend(friendID: String) {
        val userID = auth.currentUser?.uid ?: return

        db.collection("users").document(userID).collection("friends").document(friendID)
            .delete()
            .addOnSuccessListener {
                Log.d("FriendListAdapter", "Book deleted successfully")
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_list_item, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        Log.d("FriendListAdapter", "Binding view holder at position: $position")
        val currentItem = friendList[position]
        val profilePictureID = currentItem.profilePictureID

        holder.username.text = currentItem.username ?: "Username"
        holder.booksCollected.text = currentItem.booksCollected ?: "booksCollected: error"

        if (profilePictureID != null && profilePictureID != 0) {
            holder.profilePicture.setImageResource(profilePictureID)
        } else {
            holder.profilePicture.setImageResource(R.drawable.profile)
        }

        //set click listener
        holder.itemView.setOnClickListener {
            Log.d("FriendListAdapter", "Friend clicked: $position")
            if (position != RecyclerView.NO_POSITION) {
                val friendInfo = friendList[position]
                onFriendClickListener.onFriendClick(friendInfo)
            }
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    fun deleteItem(position: Int) {
        friendList.removeAt(position)
        notifyItemRemoved(position)
        onItemDeletedListener?.onItemDeleted()
    }

    interface OnItemDeletedListener {
        fun onItemDeleted()
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.username)
        val booksCollected: TextView = itemView.findViewById(R.id.booksCollected)
        val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
    }

    interface OnFriendClickListener {
        fun onFriendClick(friendInfo: FriendInfo)
    }

    class SwipeToDeleteCallback(private val adapter: FriendListAdapter) : ItemTouchHelper
    .SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView, viewHolder: RecyclerView
            .ViewHolder, target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val friendToDelete = adapter.friendList[position].userID ?: ""
            adapter.deleteItem(position)
            adapter.deleteFriend(friendToDelete)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            RecyclerViewSwipeDecorator.Builder(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
                .addBackgroundColor(ContextCompat.getColor(recyclerView.context, R.color.red))
                .addActionIcon(R.drawable.delete)
                .create()
                .decorate()
            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }

    }

}