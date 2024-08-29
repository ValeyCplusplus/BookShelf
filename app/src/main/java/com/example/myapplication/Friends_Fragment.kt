package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Friends_Fragment : Fragment() {

    private lateinit var checkUsernameButton: ImageButton
    private lateinit var usernameInput : EditText

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val usernamesCollection = db.collection("users")

    private lateinit var friendAdapter : FriendAdapter
    private val friendsList = mutableListOf<Friend>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        auth = Firebase.auth

        usernameInput = view.findViewById(R.id.inputUsername)

        checkUsernameButton = view.findViewById(R.id.checkUsernameButton)
        checkUsernameButton.setOnClickListener{ checkUsername() }

        friendAdapter = FriendAdapter(friendsList)

        val recyclerView: RecyclerView = view.findViewById(R.id.friendsRecyclerView)
        recyclerView.adapter = friendAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inflate the layout for this fragment
        return view
    }


    private fun checkUsername() {
        lifecycleScope.launch { // Launch a coroutine
            if (!verifyUsername()){
                val enteredUsername = usernameInput.text.toString()
                val newFriend = Friend(enteredUsername, 0)
                friendAdapter.addUsername(newFriend)
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

        // Check if username is taken (using coroutines)
        return try {
            val querySnapshot = usernamesCollection
                .whereEqualTo("username", enteredUsername)
                .get()
                .await()
            if (querySnapshot.documents.isEmpty()) {
                true // Username is available
            } else {

                false // Username is taken
            }
        } catch (exception: Exception) {
            // Handle the exception (e.g., log it)
            Log.e("Firestore", "Error checking username", exception)
            false // Assume not available in case of error
        }
    }

}