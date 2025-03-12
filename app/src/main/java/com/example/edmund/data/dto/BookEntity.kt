package com.example.edmund.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.edmund.domain.library.Category
import java.io.Serializable

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
    val category: Category
) : Serializable
