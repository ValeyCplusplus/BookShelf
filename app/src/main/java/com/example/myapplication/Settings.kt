package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Settings : AppCompatActivity() {

    //UI-Elements
    private lateinit var showOptionsButton: ImageButton
    private lateinit var openMenuButton: ImageButton
    private lateinit var openFriendsButton: LinearLayout
    private lateinit var openWishlistButton: LinearLayout
    private lateinit var openSettingsButton: LinearLayout
    private lateinit var openBookShelfButton: LinearLayout
    private lateinit var logOutButton: Button

    //UI-Menu-Elements
    private lateinit var menu: FrameLayout
    private lateinit var menuLayout: View
    private lateinit var closeMenuButton: ImageButton

    //Boolean
    private var addBookClicked = false

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Initialize Views
        showOptionsButton = findViewById(R.id.showOptionsButton)
        openMenuButton = findViewById(R.id.openMenuButton)
        menu = findViewById(R.id.menu)
        menuLayout = findViewById(R.id.mainMenuLayout)
        closeMenuButton = menuLayout.findViewById(R.id.closeMenuButton)
        openBookShelfButton = menuLayout.findViewById(R.id.openBookShelfButton)
        openFriendsButton = menuLayout.findViewById(R.id.openFriendsButton)
        openWishlistButton = menuLayout.findViewById(R.id.openWishlistButton)
        openSettingsButton = menuLayout.findViewById(R.id.openSettingsButton)
        logOutButton = findViewById(R.id.logOutButton)

        //Set Click Listeners
        showOptionsButton.setOnClickListener {onAddBookButtonClicked()}
        openMenuButton.setOnClickListener { onOpenMenuButtonClicked() }
        closeMenuButton.setOnClickListener { onCloseMenuButtonClicked() }
        openFriendsButton.setOnClickListener {onOpenFriendsButtonClicked()}
        openWishlistButton.setOnClickListener {onOpenWishlistButtonClicked()}
        openSettingsButton.setOnClickListener {onOpenSettingsButtonClicked()}
        openBookShelfButton.setOnClickListener {onOpenBookShelfButtonClicked()}
        logOutButton.setOnClickListener {onLogOutButtonClicked()}
        
        auth = Firebase.auth
    }

    private fun onLogOutButtonClicked() {
        auth.signOut()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    private fun onOpenBookShelfButtonClicked() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    private fun onOpenFriendsButtonClicked() {
        val intent = Intent(this, Friends::class.java)
        startActivity(intent)
    }
    private fun onOpenWishlistButtonClicked() {
        val intent = Intent(this, Wishlist::class.java)
        startActivity(intent)
    }
    private fun onOpenSettingsButtonClicked() {

    }

    private fun onOpenMenuButtonClicked()
    {
        showOptionsButton.visibility = View.GONE

        handleOptions(addBookClicked)
        addBookClicked = !addBookClicked

        menu.visibility = View.VISIBLE
    }

    private fun onCloseMenuButtonClicked()
    {
        menu.visibility = View.GONE

        showOptionsButton.visibility = View.VISIBLE
    }

    private fun onAddBookButtonClicked()
    {
        handleOptions(addBookClicked)
        addBookClicked = !addBookClicked
    }

    private fun handleOptions(clicked: Boolean)
    {
        if (!clicked)
        {
            openMenuButton.visibility = View.VISIBLE

            openMenuButton.isClickable = true
        }
        else
        {
            openMenuButton.visibility = View.GONE

            openMenuButton.isClickable = false
        }
    }
}