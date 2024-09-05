package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NavigationDrawer : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, BookListAdapter.OnItemDeletedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var headerView: View
    private lateinit var usernameTextView: TextView


    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")
    private val currentUser: FirebaseUser? get() = auth.currentUser





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_navigation_drawer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        drawerLayout = findViewById(R.id.main_drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        drawerLayout = findViewById(R.id.main_drawer_layout)
        headerView = navigationView.getHeaderView(0)
        usernameTextView = headerView.findViewById(R.id.username)

        setHeaderValues()
    }

    override fun onStart() {
        super.onStart()
        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_container, BookShelfFragment())
            .commit()
    }

    fun setHeaderValues() {
        usersCollection.document(currentUser?.uid ?: "").get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists())
                {
                    val username = documentSnapshot.getString("username")
                    usernameTextView.text = username
                }
            }
        usersCollection.document(currentUser?.uid ?: "").collection("collectedBooks").get()
            .addOnSuccessListener { querySnapshot ->
                val booksCollected = querySnapshot.size()
                val booksCollectedTextView = headerView.findViewById<TextView>(R.id.booksCollectedTextView)
                booksCollectedTextView.text = "Books: " + booksCollected.toString()
            }
            .addOnFailureListener{exception ->
                Log.w("NavigationDrawer", "Error getting documents", exception)
            }
    }

    override fun onItemDeleted()
    {
        setHeaderValues()
    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        drawerLayout.closeDrawer(GravityCompat.START)

        when (item.itemId)
        {
            R.id.nav_bookshelf ->
                {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.activity_container, BookShelfFragment())
                        .commit()
                }
            R.id.nav_friends ->
                {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.activity_container, FriendsFragment())
                        .commit()
                }
            R.id.nav_wishlist ->
                {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.activity_container, WishlistFragment())
                        .commit()
                }
            R.id.nav_settings ->
                {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.activity_container, SettingsFragment())
                        .commit()
                }
            R.id.nav_logout ->
                {
                    auth.signOut()
                    startActivity(Intent(this, Login::class.java))
                }
        }
        return true
    }
}