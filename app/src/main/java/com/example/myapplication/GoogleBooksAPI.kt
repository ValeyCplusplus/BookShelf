package com.example.myapplication

import com.google.gson.annotations.SerializedName // For handling JSON serialization with Gson
import retrofit2.Response // For handling Retrofit responses
import retrofit2.Retrofit // For creating a Retrofit instance
import retrofit2.converter.gson.GsonConverterFactory // For converting JSONresponses to Kotlin objects
import retrofit2.http.GET // For defining HTTP GET requests
import retrofit2.http.Query // For adding query parameters to requests
import kotlinx.coroutines.CoroutineScope // For using coroutines
import kotlinx.coroutines.Dispatchers // For specifying coroutine dispatchers
import kotlinx.coroutines.launch // For launching coroutines

class GoogleBooksAPI {

    interface GoogleBooksService
    {
        @GET("volumes")
        suspend fun searchByISBN(
            @Query("q") query: String,
            @Query("key") apiKey: String
        ): Response<BookSearchResult>

        @GET("volumes")
        suspend fun searchBooks(
            @Query("q") query: String,
            @Query("key") apiKey: String
        ): Response<BookSearchResult>
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/books/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val googleBooksService = retrofit.create(GoogleBooksService::class.java)

    suspend fun searchBooks(query: String, apiKey: String): Response<BookSearchResult> {
        return googleBooksService.searchBooks(query, apiKey)
    }

    suspend fun searchByISBN(isbn: String, apiKey: String): Response<BookSearchResult> {
        return googleBooksService.searchByISBN("isbn:$isbn", apiKey)
    }
}