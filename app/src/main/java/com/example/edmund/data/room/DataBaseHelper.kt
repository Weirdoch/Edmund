package com.example.edmund.data.room

import android.content.Context
import androidx.room.Room

object DatabaseHelper {

    private var INSTANCE: BookDatabase? = null

    //获取表管理类AppDatabase

    // 获取数据库实例
    fun getDatabase(context: Context): BookDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                BookDatabase::class.java,
                "book_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }

    // 清除所有数据（可选）
    suspend fun clearAllData(context: Context) {
        val db = getDatabase(context)
        db.bookDao.deleteAllHistory()
        db.bookDao.getAllBooks().forEach {
            db.bookDao.deleteBook(it)
        }
    }
}
