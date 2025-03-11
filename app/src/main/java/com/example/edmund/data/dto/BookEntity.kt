package com.example.edmund.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity
data class BookEntity(
    @PrimaryKey(true) val id: Int = 0,
    val title: String,
    val author: String?,
    val description: String?,
    val filePath: String,
    val scrollIndex: Int,
    val scrollOffset: Int,
    val progress: Float,
    val image: String? = null,
    val category: Locale.Category
)
