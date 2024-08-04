package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class BookSearchResult(
    val kind: String,
    val totalItems: Int,
    val items: List<BookItem>
)

data class BookItem(
    @SerializedName("volumeInfo") val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String? = null,
    var authors: List<String>? = null,
    val publishedDate: String? = null,
    val categories: List<String>? = null,
    val pageCount: Int? = null,
    val thumbnail: String? = null,
    val imageLinks: ImageLinks? = null,
    val industryIdentifiers: List<IndustryIdentifiers>? = null,
    val isbn: String? = null
)

data class IndustryIdentifiers(
    val type: String,
    val identifier: String
)

data class ImageLinks(
    val smallThumbnail: String?,
    val thumbnail: String?
)
