package com.example.myapplication

data class Friend(
    val username: String,
    val booksCollected: Int,
    val profilePicture: String? = null
)