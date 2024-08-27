package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import android.app.Activity
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    //UI-Elements
    private lateinit var addBookButton: ImageButton
    private lateinit var openCameraButton: ImageButton
    private lateinit var inputISBNText: EditText
    private lateinit var checkISBNButton: ImageButton
    private lateinit var openMenuButton: ImageButton
    private lateinit var openFriendsButton: LinearLayout
    private lateinit var openWishlistButton: LinearLayout
    private lateinit var openSettingsButton: LinearLayout
    private lateinit var openBookShelfButton: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var sortTitle: Button
    private lateinit var sortAuthor: Button
    private lateinit var sortPages: Button

    private lateinit var usernameTextView: TextView

    //UI-Menu-Elements
    private lateinit var menu: FrameLayout
    private lateinit var menuLayout: View
    private lateinit var closeMenuButton: ImageButton

    //Boolean
    private var addBookClicked = false

    //APIS
    private val googleBooksAPI = GoogleBooksAPI()

    //Camera and BarcodeScanner
    private lateinit var barcodeScanner: BarcodeScanner

    //other
    private lateinit var currentISBN: String
    private lateinit var adapter: BookListAdapter
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Initialize Views
        addBookButton = findViewById(R.id.showOptionsButton)
        openCameraButton = findViewById(R.id.openCameraButton)
        inputISBNText = findViewById(R.id.inputISBN_txt)
        checkISBNButton = findViewById(R.id.checkISBNButton)
        openMenuButton = findViewById(R.id.openMenuButton)
        menu = findViewById(R.id.menu)
        menuLayout = findViewById(R.id.mainMenuLayout)
        closeMenuButton = menuLayout.findViewById(R.id.closeMenuButton)
        openBookShelfButton = menuLayout.findViewById(R.id.openBookShelfButton)
        openFriendsButton = menuLayout.findViewById(R.id.openFriendsButton)
        openWishlistButton = menuLayout.findViewById(R.id.openWishlistButton)
        openSettingsButton = menuLayout.findViewById(R.id.openSettingsButton)
        usernameTextView = menuLayout.findViewById(R.id.username)
        recyclerView = findViewById(R.id.booklist)
        sortTitle = findViewById(R.id.sortTitle)
        sortAuthor = findViewById(R.id.sortAuthor)
        sortPages = findViewById(R.id.sortPages)

        //BarcodeScanner and Camera
        barcodeScanner = BarcodeScanning.getClient()

        //Set Click Listeners
        addBookButton.setOnClickListener {onAddBookButtonClicked()}
        openCameraButton.setOnClickListener {onOpenCameraButtonClicked()}

        openMenuButton.setOnClickListener { onOpenMenuButtonClicked() }
        closeMenuButton.setOnClickListener { onCloseMenuButtonClicked() }
        openFriendsButton.setOnClickListener {onOpenFriendsButtonClicked()}
        openWishlistButton.setOnClickListener {onOpenWishlistButtonClicked()}
        openSettingsButton.setOnClickListener {onOpenSettingsButtonClicked()}

        adapter = BookListAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        sortTitle.setOnClickListener {
            adapter.sortByTitle()
        }
        sortAuthor.setOnClickListener {
            adapter.sortByAuthor()
        }
        sortPages.setOnClickListener {
            adapter.sortByPages()
        }


        val swipeHandler = ItemTouchHelper(BookListAdapter.SwipeToDeleteCallback(adapter))
        swipeHandler.attachToRecyclerView(recyclerView )
    }

    override fun onStart() {
        super.onStart()

        adapter.loadBooks()

        checkISBNButton.setOnClickListener()
        {
            val isbn = inputISBNText.text.toString()
            if (isbn.length == 13)
            {
                currentISBN = isbn
                searchByISBN(currentISBN)
            }
            else
            {
                val message = if (isbn.isEmpty()) "Please enter an ISBN"
                            else if (isbn.length < 13) "ISBN is too short"
                            else "ISBN Is too long"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = Firebase.auth.currentUser?.uid ?: ""
        if (userId.isNotEmpty()) {
            db.collection("usernames").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username") ?: "no Username Set"
                        usernameTextView.text = username
                    } else {
                        usernameTextView.text = "no Username Set"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MainActivity", "Error getting username: ", exception)
                    usernameTextView.text = "Error loading username"
                }
        } else {
            usernameTextView.text = "Not logged in"
        }
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
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
    }

    private fun onOpenMenuButtonClicked()
    {
        addBookButton.visibility = View.GONE

        handleOptions(addBookClicked)
        addBookClicked = !addBookClicked

        menu.visibility = View.VISIBLE
    }

    private fun onCloseMenuButtonClicked()
    {
        menu.visibility = View.GONE

        addBookButton.visibility = View.VISIBLE
    }

    private fun onAddBookButtonClicked()
    {
        handleOptions(addBookClicked)
        addBookClicked = !addBookClicked
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val barcodeValue = intent?.getStringExtra("barcode") ?:""

            if (barcodeValue.length == 13) {
                currentISBN = barcodeValue
                searchByISBN(currentISBN)
                Toast.makeText(this, barcodeValue, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onOpenCameraButtonClicked() {
        val intent = Intent(this, Camera::class.java)
        startForResult.launch(intent)
    }


    private fun searchByISBN(isbn: String) {
        CoroutineScope(Dispatchers.IO).launch {

            if (isInBookList(adapter.bookList, isbn))
            {
                withContext(Dispatchers.Main)
                {
                    Toast.makeText(this@MainActivity, "Book already in list", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val response = googleBooksAPI.searchByISBN(isbn, getString(R.string.googlebooksapi_key))
            if (response.isSuccessful) {
                val bookSearchResult = response.body()
                // Switch to the main thread to update UI
                withContext(Dispatchers.Main) {

                    val bookItems = bookSearchResult?.items?.map { item ->
                        val isbn13 = item.volumeInfo.industryIdentifiers?.firstOrNull { it.type == "ISBN_13" }?.identifier ?: ""
                        VolumeInfo(
                            title = item.volumeInfo.title,
                            authors = item.volumeInfo.authors,
                            publishedDate = item.volumeInfo.publishedDate,
                            pageCount = item.volumeInfo.pageCount ?: 0,
                            categories = item.volumeInfo.categories,
                            thumbnail = item.volumeInfo.imageLinks?.thumbnail,
                            industryIdentifiers = item.volumeInfo.industryIdentifiers,
                            isbn = isbn13
                        )
                    } ?: emptyList()

                    adapter.bookList.addAll(bookItems)

                    for (book in bookItems)
                    {
                        adapter.saveBook(book)
                    }

                    adapter.notifyItemRangeInserted(adapter.bookList.size - bookItems.size, bookItems.size)
                }
            } else {
                // Handle API error, e.g., show an error message
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error fetching book details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isInBookList (bookList: List<VolumeInfo>, isbn: String): Boolean
    {
        return bookList.any { it.isbn == isbn }
    }

    private fun handleOptions(clicked: Boolean)
    {
        if (!clicked)
        {
            inputISBNText.visibility = View.VISIBLE
            checkISBNButton.visibility = View.VISIBLE
            openCameraButton.visibility = View.VISIBLE
            openMenuButton.visibility = View.VISIBLE

            inputISBNText.isClickable = true
            checkISBNButton.isClickable = true
            openCameraButton.isClickable = true
            openMenuButton.isClickable = true
        }
        else
        {
            inputISBNText.visibility = View.GONE
            checkISBNButton.visibility = View.GONE
            openCameraButton.visibility = View.GONE
            openMenuButton.visibility = View.GONE

            inputISBNText.isClickable = false
            checkISBNButton.isClickable = false
            openCameraButton.isClickable = false
            openMenuButton.isClickable = false
        }
    }
}