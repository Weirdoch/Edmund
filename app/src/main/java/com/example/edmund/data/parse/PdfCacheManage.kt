package com.example.edmund.data.parse

import android.graphics.Bitmap
import android.util.LruCache
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PdfCacheManager {

    private val pdfCache: LruCache<Int, Bitmap> = LruCache(20) // 根据内存大小设置缓存大小

    /**
     * 获取缓存的 Bitmap，如果没有则创建并缓存
     */
    private val lock = Any()

    suspend fun getPageBitmap(
        pdfiumCore: PdfiumCore,
        pdfDocument: PdfDocument,
        pageIndex: Int,
        width: Int,
        height: Int
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            synchronized(lock) {
                // 如果缓存中已有该页面，直接返回缓存的 Bitmap
                pdfCache.get(pageIndex)?.let {
                    it
                } ?: run {
                    // 创建并缓存页面
                    val bitmap = createCache(pdfiumCore, pdfDocument, pageIndex, width, height)
                    bitmap
                }
            }
        }
    }


    // 创建缓存
    fun createCache(
        pdfiumCore: PdfiumCore,
        pdfDocument: PdfDocument,
        pageIndex: Int,
        width: Int,
        height: Int
    ): Bitmap {
        if (width > 0 && height > 0) {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            pdfiumCore.openPage(pdfDocument, pageIndex)
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageIndex, 0, 0, width, height)
            pdfCache.put(pageIndex, bitmap) // 缓存该页面
            return bitmap
        }
        return Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
    }

    // 预加载相邻页面
    // 使用指定的 scope，而不是 GlobalScope
    public fun cacheAdjacentPages(
        currentPage: Int,
        pdfiumCore: PdfiumCore,
        pdfDocument: PdfDocument,
        width: Int,
        height: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val totalPageCount = pdfiumCore.getPageCount(pdfDocument)
            if (currentPage < totalPageCount - 1 && pdfCache[currentPage + 1] == null) {
                createCache(pdfiumCore, pdfDocument, currentPage + 1, width, height) // 下一页
            }
            if (currentPage + 2 < totalPageCount && pdfCache[currentPage + 2] == null) {
                createCache(pdfiumCore, pdfDocument, currentPage + 2, width, height) // 下一页的下一页
            }
        }
    }

    // 清空缓存
    fun clear() {
        pdfCache.evictAll()
    }

    // 获取当前缓存的大小
    fun getCacheSize(): Int {
        return pdfCache.size()
    }
}

