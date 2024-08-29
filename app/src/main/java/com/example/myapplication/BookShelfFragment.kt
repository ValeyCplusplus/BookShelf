package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookShelfFragment : Fragment() {

    //UI-Elements
    private lateinit var addBookButton: ImageButton
    private lateinit var openCameraButton: ImageButton
    private lateinit var inputISBNText: EditText
    private lateinit var checkISBNButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var sortTitle: Button
    private lateinit var sortAuthor: Button
    private lateinit var sortPages: Button

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_shelf, container, false)

        //Initialize Views
        addBookButton = view.findViewById(R.id.showOptionsButton)
        openCameraButton = view.findViewById(R.id.openCameraButton)
        inputISBNText = view.findViewById(R.id.inputISBN)
        checkISBNButton = view.findViewById(R.id.checkISBNButton)
        recyclerView = view.findViewById(R.id.booklistRecyclerView)
        sortTitle = view.findViewById(R.id.sortTitle)
        sortAuthor = view.findViewById(R.id.sortAuthor)
        sortPages = view.findViewById(R.id.sortPages)

        //BarcodeScanner and Camera
        barcodeScanner = BarcodeScanning.getClient()

        //Set Click Listeners
        addBookButton.setOnClickListener {onAddBookButtonClicked()}
        openCameraButton.setOnClickListener {onOpenCameraButtonClicked()}

        adapter = BookListAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
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

        return view
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
                Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
            }
        }
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
                Toast.makeText(requireActivity(), barcodeValue, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onOpenCameraButtonClicked() {
        val intent = Intent(requireActivity(), Camera::class.java)
        startForResult.launch(intent)
    }


    private fun searchByISBN(isbn: String) {
        CoroutineScope(Dispatchers.IO).launch {

            if (isInBookList(adapter.bookList, isbn))
            {
                withContext(Dispatchers.Main)
                {
                    Toast.makeText(requireActivity(), "Book already in list", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireActivity(), "Error fetching book details", Toast.LENGTH_SHORT).show()
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

            inputISBNText.isClickable = true
            checkISBNButton.isClickable = true
            openCameraButton.isClickable = true
        }
        else
        {
            inputISBNText.visibility = View.GONE
            checkISBNButton.visibility = View.GONE
            openCameraButton.visibility = View.GONE

            inputISBNText.isClickable = false
            checkISBNButton.isClickable = false
            openCameraButton.isClickable = false
        }
    }

}