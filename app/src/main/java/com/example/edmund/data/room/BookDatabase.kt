package com.example.edmund.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.data.dto.HistoryEntity

@Database(
    entities = [BookEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class BookDatabase : RoomDatabase() {

    abstract val bookDao: BookDao
}
