package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Wishlist : AppCompatActivity() {

    //UI-Elements
    private lateinit var showOptionsButton: ImageButton
    private lateinit var inputUsernameText: EditText
    private lateinit var checkUsernameButton: ImageButton
    private lateinit var openMenuButton: ImageButton
    private lateinit var openFriendsButton: LinearLayout
    private lateinit var openWishlistButton: LinearLayout
    private lateinit var openSettingsButton: LinearLayout
    private lateinit var openBookShelfButton: LinearLayout

    //UI-Menu-Elements
    private lateinit var menu: FrameLayout
    private lateinit var menuLayout: View
    private lateinit var closeMenuButton: ImageButton

    //Boolean
    private var addBookClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_wishlist)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Initialize Views
        showOptionsButton = findViewById(R.id.showOptionsButton)
        inputUsernameText = findViewById(R.id.inputUsernameText)
        checkUsernameButton = findViewById(R.id.checkUsernameButton)
        openMenuButton = findViewById(R.id.openMenuButton)
        menu = findViewById(R.id.menu)
        menuLayout = findViewById(R.id.mainMenuLayout)
        closeMenuButton = menuLayout.findViewById(R.id.closeMenuButton)
        openBookShelfButton = menuLayout.findViewById(R.id.openBookShelfButton)
        openFriendsButton = menuLayout.findViewById(R.id.openFriendsButton)
        openWishlistButton = menuLayout.findViewById(R.id.openWishlistButton)
        openSettingsButton = menuLayout.findViewById(R.id.openSettingsButton)

        //Set Click Listeners
        showOptionsButton.setOnClickListener {onAddBookButtonClicked()}
        checkUsernameButton.setOnClickListener { onCheckUsernameButtonClicked() }
        openMenuButton.setOnClickListener { onOpenMenuButtonClicked() }
        closeMenuButton.setOnClickListener { onCloseMenuButtonClicked() }
        openFriendsButton.setOnClickListener {onOpenFriendsButtonClicked()}
        openWishlistButton.setOnClickListener {onOpenWishlistButtonClicked()}
        openSettingsButton.setOnClickListener {onOpenSettingsButtonClicked()}
        openBookShelfButton.setOnClickListener {onOpenBookShelfButtonClicked()}
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

    }
    private fun onOpenSettingsButtonClicked() {
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
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

    private fun onCheckUsernameButtonClicked()
    {
        if (inputUsernameText.text.toString().length == 13)
        {
            Toast.makeText(this, "Username is valid", Toast.LENGTH_SHORT).show()
        }
        else if (inputUsernameText.text.toString().length < 13)
        {
            Toast.makeText(this, "Username is too short", Toast.LENGTH_SHORT).show()
        }
        else if (inputUsernameText.text.toString().length > 13)
        {
            Toast.makeText(this, "Username is too long", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleOptions(clicked: Boolean)
    {
        if (!clicked)
        {
            inputUsernameText.visibility = View.VISIBLE
            checkUsernameButton.visibility = View.VISIBLE
            openMenuButton.visibility = View.VISIBLE

            inputUsernameText.isClickable = true
            checkUsernameButton.isClickable = true
            openMenuButton.isClickable = true
        }
        else
        {
            inputUsernameText.visibility = View.GONE
            checkUsernameButton.visibility = View.GONE
            openMenuButton.visibility = View.GONE

            inputUsernameText.isClickable = false
            checkUsernameButton.isClickable = false
            openMenuButton.isClickable = false
        }
    }
}