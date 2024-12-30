package com.song.thiefread.model

data class Book(
    val path: String,
    val title: String,
    val author: String = "",
    val chapters: List<Chapter> = emptyList()
)

data class Chapter(
    val title: String,
    val content: String,
    val index: Int
) 