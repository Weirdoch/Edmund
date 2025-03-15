package com.example.edmund.data.parse

import android.graphics.Bitmap
import android.util.LruCache

class PdfCache(private val maxSize: Int = 20) {

    private val cache: LruCache<Int, Bitmap> = LruCache(maxSize)

    // 获取缓存中的页面
    fun getPage(pageNumber: Int): Bitmap? {
        return cache.get(pageNumber)
    }

    // 将页面缓存到内存
    fun putPage(pageNumber: Int, pageBitmap: Bitmap) {
        cache.put(pageNumber, pageBitmap)
    }

    // 清空缓存
    fun clear() {
        cache.evictAll()
    }
}
