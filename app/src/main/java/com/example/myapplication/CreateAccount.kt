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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class CreateAccount : AppCompatActivity() {

    //UI-Elements
    private lateinit var username: EditText
    private lateinit var createAccountButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

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


        auth = Firebase.auth

    }

    private fun createAccount() {

        auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Account creation success
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(verifyUsername())
                        .build()
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener{ profileTask ->
                            if (profileTask.isSuccessful) {

                                db.collection("usernames").document(user?.uid ?: "")
                                    .set(hashMapOf(
                                        "email" to email.text.toString(),
                                        "username" to username.text.toString()
                                    )).addOnSuccessListener {
                                        Toast.makeText(this, "Username set successfully", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Username set failed", Toast.LENGTH_SHORT).show()
                                    }

                            }
                            else {
                                Toast.makeText(this, "Username set failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Account creation failed
                    Toast.makeText(this, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun verifyUsername(): String? {
        if (username.text.toString().isEmpty())
        {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return null
        }
        else if (username.text.toString().length in 5..14)
        {
            return username.text.toString()
        }
        else
        {
            Toast.makeText(this, "Username must be between 6 and 14 characters", Toast.LENGTH_SHORT).show()
            return null
        }
    }
}