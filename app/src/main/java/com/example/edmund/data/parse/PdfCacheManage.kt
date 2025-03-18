package com.example.edmund.data.parse

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import java.io.Serializable


class PdfCacheManager (
    val pdfiumCore: PdfiumCore,
    val pdfDocument: PdfDocument,
    val totalPageCount: Int
) {

    private val pdfCache: LruCache<Int, Bitmap> = LruCache(20) // 高清图像缓存
    private val emptyBitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var height = 0
    private var width = 0

    private val lock = Any()

    init {
        pdfiumCore.openPage(pdfDocument, 0)
        width = pdfiumCore.getPageWidth(pdfDocument, 0)
        height = pdfiumCore.getPageHeight(pdfDocument, 0)
    }



    /**
     * 获取缓存的 Bitmap，如果没有则创建并缓存
     */
    fun getPageBitmap(pageIndex: Int): Bitmap {
        if (pageIndex < totalPageCount) {
            // 尝试从缓存中获取 Bitmap
            pdfCache[pageIndex]?.let {
                return it
            }

            // 如果缓存中没有，则创建并缓存
            return createCache(pageIndex)
        }
        return emptyBitmap
    }


    fun getLeftPageBitmap(pageIndex: Int): Bitmap {
        val leftIndex = pageIndex*2
        return getPageBitmap(leftIndex)
    }

    fun getRightPageBitmap(pageIndex: Int): Bitmap {
        val rightIndex = pageIndex*2+1
        if (!hasPage(rightIndex + 1)) {
            Thread {
                // 这个代码块将在新线程中执行
                Log.d("PdfCacheManager", "预缓存 ${pageIndex + 1}")
                cacheNextPages(rightIndex) }.start()
        }
        return getPageBitmap(rightIndex)
    }

    /**
     * 创建缓存并返回 Bitmap
     */
    private fun createCache(pageIndex: Int): Bitmap {
        // 防止多次创建相同的缓存
        if (pageIndex >= totalPageCount || pdfCache[pageIndex] != null) {
            return emptyBitmap
        }

        synchronized(lock) {
            // 再次检查以防其他线程已经创建了该页面缓存
            pdfCache[pageIndex]?.let {
                return it
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            pdfiumCore.openPage(pdfDocument, pageIndex)
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageIndex, 0, 0, width, height)
            pdfCache.put(pageIndex, bitmap) // 缓存该页面
            return bitmap
        }
    }

    fun hasPage(pageIndex: Int): Boolean {
        return pdfCache[pageIndex] != null
    }

    /**
     * 预加载页面
     */
    fun cacheNextPages(currentPage: Int) {
        if (currentPage + 2 <= totalPageCount) {
            preCachePages(currentPage + 1, currentPage + 2)
        } else if (currentPage + 1 <= totalPageCount) {
            preCachePages(currentPage + 1, currentPage + 1)
        }
    }

    /**
     * 批量预加载页面
     */
    // 修改PdfCacheManager中的preCachePages方法
    fun preCachePages(startPage: Int, endPage: Int) {
        // 确保页面范围有效
        val validStart = startPage.coerceAtLeast(0)
        val validEnd = endPage.coerceAtMost(totalPageCount - 1)

        for (page in validStart..validEnd) {
            if (pdfCache.get(page) == null) {
                createCache(page)
            }
        }
    }

    /**
     * 清空缓存
     */
    fun clear() {
        pdfCache.evictAll()
    }

    /**
     * 获取当前缓存的大小
     */
    fun getCacheSize(): Int {
        return pdfCache.size()
    }


    fun evictOldestPages() {
        // 获取缓存的所有项
        val snapshot = pdfCache.snapshot()

        // 如果缓存不为空，则删除最老的页面（即访问次数最少的页面）
        val oldestKey = snapshot.keys.firstOrNull()
        oldestKey?.let {
            pdfCache.remove(it) // 删除最老的页面
        }
    }

}

