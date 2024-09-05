package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateAccount : AppCompatActivity() {

    //UI-Elements
    private lateinit var username: EditText
    private lateinit var createAccountButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val usernamesCollection = db.collection("users")


    private var selectedImageId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Initialize Views
        username = findViewById(R.id.usernameCreateAccount)
        createAccountButton = findViewById(R.id.createNewAccountButton)
        email = findViewById(R.id.emailAddressCreateAccount)
        password = findViewById(R.id.firstPasswordCreateAccount)
        confirmPassword = findViewById(R.id.repeatPasswordCreateAccount)

        //Set Click Listeners
        createAccountButton.setOnClickListener { createAccount() }

        val viewPager = findViewById<ViewPager2>(R.id.profilePictureViewPager)
        val imageResourceIds = listOf(
            R.drawable.profile_1, // Replace with your actual image resource IDs
            R.drawable.profile_2,
            R.drawable.profile_3
        )

        val adapter = ProfilePicturePagerAdapter(this, imageResourceIds)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectedImageId = imageResourceIds[viewPager.currentItem]
            }
        })
        auth = Firebase.auth

    }

    private fun createAccount() {
        lifecycleScope.launch { // Launch a coroutine
            if (verifyEmail() && verifyPassword() && verifyUsername()){
                // Now you can call verifyUsername() within the coroutine context
                auth.createUserWithEmailAndPassword(
                    email.text.toString(),
                    confirmPassword.text.toString()
                ).addOnCompleteListener(this@CreateAccount) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid

                        userId?.let {
                            val usernameData = hashMapOf(
                                "userID" to it,
                                "username" to username.text.toString(),
                                "profilePictureID" to selectedImageId,
                                "booksCollected" to "0"
                            )

                            usernamesCollection.document(it)
                                .set(usernameData)
                                .addOnSuccessListener {
                                    Toast.makeText(this@CreateAccount, "Username saved", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener{
                                    Toast.makeText(this@CreateAccount, "Error saving username", Toast.LENGTH_SHORT).show()
                                }
                        }

                        val intent = Intent(this@CreateAccount, NavigationDrawer::class.java)
                        startActivity(intent)
                        Toast.makeText(this@CreateAccount, "Account created.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this@CreateAccount,
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private suspend fun verifyUsername(): Boolean {
        val enteredUsername = username.text.toString()
        if (enteredUsername.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return false
        } else if (enteredUsername.length !in 6..14) {
            Toast.makeText(this, "Username must be between 6 and 14 characters", Toast.LENGTH_SHORT).show()
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
            }
            else {
                Toast.makeText(this, "Username is already taken", Toast.LENGTH_SHORT).show()
                false // Username is taken
            }
        } catch (exception: Exception) {
            // Handle the exception (e.g., log it)
            Toast.makeText(this, "Error checking username", Toast.LENGTH_SHORT).show()
            false // Assume not available in case of error
        }
    }

    private fun verifyPassword(): Boolean {
        val enteredPassword = password.text.toString()
        val confirmedPassword = confirmPassword.text.toString()

        if (enteredPassword != confirmedPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (enteredPassword.length < 6) { // Stronger password requirement
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true // Password is valid
    }

    private fun verifyEmail(): Boolean {
        val enteredEmail = email.text.toString()
        // Basic email validation (you can add more robust checks if needed)
        return enteredEmail.contains("@") && enteredEmail.contains(".")
    }

}