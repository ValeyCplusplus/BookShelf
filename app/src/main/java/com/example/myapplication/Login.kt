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
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class Login : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    private lateinit var emailOrUsername : EditText
    private lateinit var password : EditText

    private lateinit var loginButton : Button
    private lateinit var registerButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Initialize Views
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.createAccountButton)
        emailOrUsername = findViewById(R.id.emailOrUsernameLogin)
        password = findViewById(R.id.PasswordLogin)

        //Set Click Listeners

        registerButton.setOnClickListener(){openCreateAccountActivity()}



        auth = Firebase.auth
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null)
        {
            Toast.makeText(this, "User is logged in", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, NavigationDrawer::class.java)
            startActivity(intent)
        }
        else
        {
            loginButton.setOnClickListener(){login()}
            Toast.makeText(this, "No User is currently logged in", Toast.LENGTH_SHORT).show()

        }
    }

    private fun login() {
        val emailOrUsername = emailOrUsername.text.toString()

        if (emailOrUsername.contains("@")) {
            auth.signInWithEmailAndPassword(emailOrUsername, password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "User logged in successfully!", Toast.LENGTH_SHORT)
                            .show()
                        val user = auth.currentUser
                        val intent = Intent(this, NavigationDrawer::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
            else
            {
                val usernamesCollection = Firebase.firestore.collection("usernames")
                usernamesCollection.whereEqualTo("username", emailOrUsername).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.documents.isNotEmpty()) {
                            val document = querySnapshot.documents[0]
                            val email = document.getString("email")
                            if (email != null)
                            {
                                auth.signInWithEmailAndPassword(email, password.text.toString())
                                    .addOnCompleteListener(this) { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(this, "User logged in successfully!", Toast.LENGTH_SHORT)
                                                .show()
                                            val user = auth.currentUser
                                            val intent = Intent(this, NavigationDrawer::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                            }
                            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun openCreateAccountActivity() {
        val intent = Intent(this, CreateAccount::class.java)
        startActivity(intent)
    }
}