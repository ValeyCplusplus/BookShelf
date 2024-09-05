package com.example.myapplication

data class FriendInfo(
    val userID: String? = null,
    val username: String? = null,
    val booksCollected: String? = null,
    val profilePictureID: Int? = null
){
    constructor(
        username: String?,
        booksCollected: String?,
        profilePictureID: Int?
    ) : this(null, username, booksCollected, profilePictureID)
}