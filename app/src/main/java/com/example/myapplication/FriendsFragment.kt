package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FriendsFragment : Fragment(), FriendListAdapter.OnFriendClickListener {

    private lateinit var checkUsernameButton: ImageButton
    private lateinit var usernameInput : EditText

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    private lateinit var friendListAdapter : FriendListAdapter
    private val friendsList = mutableListOf<FriendInfo>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        auth = Firebase.auth

        usernameInput = view.findViewById(R.id.inputUsername)

        checkUsernameButton = view.findViewById(R.id.checkUsernameButton)
        checkUsernameButton.setOnClickListener{ checkUsername() }

        friendListAdapter = FriendListAdapter(friendsList, this)

        val recyclerView: RecyclerView = view.findViewById(R.id.friendsRecyclerView)
        recyclerView.adapter = friendListAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val swipeHandler = ItemTouchHelper(FriendListAdapter.SwipeToDeleteCallback(friendListAdapter))
        swipeHandler.attachToRecyclerView(recyclerView )

        friendListAdapter.loadFriends()
        recyclerView.adapter = friendListAdapter



        recyclerView.setOnClickListener {
            Log.d("FriendsFragment", "RecyclerView clicked!")
        }

        return view
    }

    override fun onFriendClick(friendInfo: FriendInfo) {
        Log.d("FriendsFragment", "Starting fragment transaction")
        val friendBookList = FriendBookList().apply {
            arguments = Bundle().apply {
                putString("friendId", friendInfo.userID)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.activity_container, friendBookList)
            .commit()
        Log.d("FriendsFragment", "Fragment transaction committed")
    }

    private fun checkUsername() {
        lifecycleScope.launch {
            if (!verifyUsername()) {
                return@launch
            }

            val enteredUsername = usernameInput.text.toString()

            try {
                val querySnapshot = usersCollection
                    .whereEqualTo("username", enteredUsername)
                    .get().await()

                if (querySnapshot.documents.isNotEmpty()) {
                    val friendDoc = querySnapshot.documents[0]
                    val friendUserId = friendDoc.id

                    val booksCollected = withContext(Dispatchers.IO) {
                        val booksSnapshot = usersCollection.document(friendUserId).collection("collectedBooks").get().await()
                        booksSnapshot.size().toString()
                    }

                    val newFriend = FriendInfo(
                        friendUserId,
                        enteredUsername,
                        booksCollected,
                        friendDoc.getLong("profilePictureID")?.toInt()
                    )

                    friendListAdapter.saveFriend(newFriend, friendUserId)
                    withContext(Dispatchers.Main) {
                        friendListAdapter.addFriend(newFriend)
                        friendListAdapter.notifyDataSetChanged()
                        usernameInput.text.clear()
                        Toast.makeText(requireContext(), "Friend added!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("FriendsFragment", "Error checking username: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "An error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private suspend fun verifyUsername(): Boolean {
        val enteredUsername = usernameInput.text.toString()
        if (enteredUsername.isEmpty()) {
            Toast.makeText(requireActivity(), "Please enter a username", Toast.LENGTH_SHORT).show()
            return false
        } else if (enteredUsername.length !in 6..14) {
            Toast.makeText(requireActivity(), "Username must be between 6 and 14 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        val currentUser = auth.currentUser ?: return false
        val currentUserDocRef = usersCollection.document(currentUser.uid)

        return try{

            val currentUsername = withContext(Dispatchers.IO) {
                val documentSnapshot = currentUserDocRef.get().await()
                documentSnapshot.getString("username") ?: ""
            }

            if (enteredUsername.equals(currentUsername, ignoreCase = true)) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireActivity(), "You cannot add yourself as a friend", Toast.LENGTH_SHORT).show()
                }
                false
            } else {

                val querySnapshot = usersCollection
                    .whereEqualTo("username", enteredUsername)
                    .get()
                    .await()
                querySnapshot.documents.isNotEmpty()
            }
        } catch (exception: Exception) {

            Log.e("Firestore", "Error checking username", exception)
            false
        }
    }

}