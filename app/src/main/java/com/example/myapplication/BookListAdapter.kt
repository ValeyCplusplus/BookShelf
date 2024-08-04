package com.example.myapplication


import android.graphics.Canvas
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.toObject
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class BookListAdapter(public val bookList: MutableList<VolumeInfo>): RecyclerView.Adapter<BookListAdapter.BookViewHolder>() {

    private val db = Firebase.firestore
    private val booksCollection = db.collection("books")

    fun saveBook(volumeInfo: VolumeInfo)
    {
        val book = hashMapOf(
            "title" to volumeInfo.title,
            "authors" to volumeInfo.authors,
            "categories" to volumeInfo.categories,
            "pageCount" to volumeInfo.pageCount,
            "publishedDate" to volumeInfo.publishedDate,
            "thumbnail" to volumeInfo.thumbnail,
            "isbn" to volumeInfo.isbn    )

        db.collection("books").document(volumeInfo.isbn?:"")
            .set(book)
            .addOnSuccessListener {
                Log.d("BookListAdapter", "Book saved successfully")
            }
            .addOnFailureListener{
                Log.w("BookListAdapter", "Error saving book", it)
            }

    }

    fun loadBooks() {
        bookList.clear()
        booksCollection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val book = document.toObject(VolumeInfo::class.java)
                    if (book != null) {
                        bookList.add(book)
                    }
                }
                notifyDataSetChanged()

            }
    }

    fun deleteBook(isbn: String)
    {
        booksCollection.document(isbn)
            .delete()
            .addOnSuccessListener {
                Log.d("BookListAdapter", "Book deleted successfully")
            }
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val title: TextView = itemView.findViewById(R.id.title)
        val author: TextView = itemView.findViewById(R.id.author)
        val category: TextView = itemView.findViewById(R.id.category)
        val pageCount: TextView = itemView.findViewById(R.id.pageCount)
        val releaseDate: TextView = itemView.findViewById(R.id.releaseDate)
        val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.book_list_item, parent, false)
        return BookViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val currentItem = bookList[position]
        holder.title.text = currentItem.title ?:"Title not found"
        holder.author.text = currentItem.authors?.joinToString(", ") ?: "Author not found"
        holder.category.text = currentItem.categories?.joinToString(", ") ?: "Category not found"
        holder.pageCount.text = currentItem.pageCount.toString() ?: "Page count not found"
        holder.releaseDate.text = currentItem.publishedDate ?: "Release date not found"

        val thumbnailUrl = currentItem.thumbnail
        if (thumbnailUrl != null){
            Glide.with(holder.itemView.context)
                .load(thumbnailUrl.replace("http", "https"))
                .into(holder.thumbnail)
        } else {
            holder.thumbnail.setImageResource(R.drawable.no_cover)
        }
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    fun sortByTitle()
    {
        bookList.sortWith(compareBy{ it.title })
        notifyDataSetChanged()
    }

    fun sortByAuthor()
    {
        bookList.sortWith(compareBy{ it.authors?.get(0) })
        notifyDataSetChanged()
    }

    fun sortByPages()
    {
        bookList.sortWith(compareBy{ it.pageCount })
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int)
    {
        bookList.removeAt(position)
        notifyItemRemoved(position)
    }

    class SwipeToDeleteCallback(private val adapter: BookListAdapter) : ItemTouchHelper
    .SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView
        .ViewHolder, target: RecyclerView.ViewHolder): Boolean{
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter.deleteItem(position)
            adapter.deleteBook(adapter.bookList[position].isbn?: "")
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addBackgroundColor(ContextCompat.getColor(recyclerView.context, R.color.red))
                .addActionIcon(R.drawable.delete)
                .create()
                .decorate()
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

    }

}