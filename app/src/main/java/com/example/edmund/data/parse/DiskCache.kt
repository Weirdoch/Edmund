package com.example.edmund.data.parse

import java.io.File
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

//class DiskCache(private val cacheDir: File) {
//
//    private val cache: DiskLruCache
//
//    init {
//        // 初始化 DiskLruCache
//        cache = DiskLruCache.open(cacheDir, 1, 1, 10 * 1024 * 1024) // 10MB
//    }
//
//    // 从磁盘缓存读取页面
//    fun getPage(pageNumber: Int): Bitmap? {
//        val key = "page_$pageNumber"
//        val snapshot = cache.get(key)
//
//        snapshot?.let {
//            val inputStream = snapshot.getInputStream(0)
//            return BitmapFactory.decodeStream(inputStream)
//        }
//        return null
//    }
//
//    // 将页面缓存到磁盘
//    fun putPage(pageNumber: Int, pageBitmap: Bitmap) {
//        val key = "page_$pageNumber"
//        val editor = cache.edit(key)
//
//        editor?.let {
//            val outputStream = editor.newOutputStream(0)
//            pageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//            outputStream.close()
//            editor.commit()
//        }
//    }
//
//    // 清除磁盘缓存
//    fun clear() {
//        cache.delete()
//    }
//}
