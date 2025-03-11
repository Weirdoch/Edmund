package com.example.edmund.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.data.dto.HistoryEntity

@Dao
interface BookDao {

    // 插入新的书籍
    @Insert
    suspend fun insertBook(book: BookEntity)

    // 根据书籍ID获取书籍
    @Query("SELECT * FROM BookEntity WHERE id = :id")
    suspend fun getBookById(id: Int): BookEntity?

    // 获取所有书籍
    @Query("SELECT * FROM BookEntity")
    suspend fun getAllBooks(): List<BookEntity>

    // 更新书籍
    @Update
    suspend fun updateBook(book: BookEntity)

    // 删除书籍
    @Delete
    suspend fun deleteBook(book: BookEntity)

    // 删除历史记录
    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    // 根据书籍ID删除历史记录
    @Query("DELETE FROM HistoryEntity WHERE bookId = :bookId")
    suspend fun deleteHistoryByBookId(bookId: Int)

    // 插入历史记录
    @Insert
    suspend fun insertHistory(history: HistoryEntity)

    // 获取指定书籍的历史记录
    @Query("SELECT * FROM HistoryEntity WHERE bookId = :bookId ORDER BY time DESC")
    suspend fun getHistoryByBookId(bookId: Int): List<HistoryEntity>

    // 删除所有历史记录
    @Query("DELETE FROM HistoryEntity")
    suspend fun deleteAllHistory()
}
