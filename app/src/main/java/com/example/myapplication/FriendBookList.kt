package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FriendBookList : Fragment() {

    private lateinit var friendBookList: RecyclerView
    private lateinit var headerViewReference: View
    private lateinit var db: FirebaseFirestore
    private lateinit var bookListAdapter: BookListAdapter
    private lateinit var friendNameTextView: TextView
    private lateinit var friendBooksCollectedTextView: TextView
    private lateinit var friendProfilePicture: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_book_list, container, false)

        friendBookList = view.findViewById(R.id.friendBookList)
        headerViewReference = view.findViewById(R.id.friendHeader)
        friendNameTextView = headerViewReference.findViewById(R.id.username)
        friendBooksCollectedTextView = headerViewReference.findViewById(R.id.booksCollectedTextView)
        friendProfilePicture = headerViewReference.findViewById(R.id.profilePicture)
        db = FirebaseFirestore.getInstance()
        bookListAdapter = BookListAdapter(mutableListOf())

        friendBookList.adapter = bookListAdapter
        friendBookList.layoutManager = LinearLayoutManager(requireContext())

        val friendId = arguments?.getString("friendId") ?: ""
        Log.d("FriendBookList", "Received friendId: $friendId")
        if (friendId.isNotEmpty()) {
            fetchFriendDetails(friendId)
            fetchCollectedBooks(friendId)
            Log.d("FriendBookList", friendId)
        } else {
            Log.e("FriendBookList", "Friend ID not found")
        }

        return view
    }

    private fun fetchFriendDetails(friendId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val friendDocument = db.collection("users").document(friendId).get().await()
            val username = friendDocument.getString("username") ?: "Unknown User"
            val profilePictureId = friendDocument.getLong("profilePictureID")?.toInt()

            val booksCollected = withContext(Dispatchers.IO) {
                val booksSnapshot = db.collection("users").document(friendId).collection("collectedBooks").get().await()
                booksSnapshot.size().toString()
            }

            withContext(Dispatchers.Main) {
                friendNameTextView.text = username
                friendBooksCollectedTextView.text = "Books Collected: $booksCollected"
                friendProfilePicture.setImageResource(profilePictureId ?: R.drawable.profile)
            }
        } catch (e: Exception) {
            Log.e("FriendBookList", "Error fetching friend details", e)
        }
    }

    private fun fetchCollectedBooks(friendId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val booksSnapshot= db.collection("users").document(friendId)
                .collection("collectedBooks").get().await()

            val bookList = booksSnapshot.documents.mapNotNull { document ->

                VolumeInfo(
                    title = document.getString("title") ?: "Unknown Title",
                    authors = document.get("authors") as? List<String> ?: listOf("Unknown Author"),
                    categories = document.get("categories") as? List<String> ?: listOf("Unknown Category"),
                    pageCount = document.getLong("pageCount")?.toInt() ?: 0,
                    publishedDate = document.getString("publishedDate") ?: "Unknown Date",
                    thumbnail = document.getString("thumbnail") ?: "",
                    isbn = document.getString("isbn") ?: ""
                )
            }.toMutableList()

            withContext(Dispatchers.Main) {
                bookListAdapter = BookListAdapter(bookList)
                friendBookList.adapter = bookListAdapter
            }
        } catch (e: Exception) {Log.e("FriendBookList", "Error fetching collected books", e)
        }
    }
}