package com.example.edmund.data.parse

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.github.barteksc.pdfviewer.PDFView
import javax.inject.Inject

//class MyPdfView(context: Context?, set: AttributeSet?) : PDFView(context, set) {
//
//    @Inject lateinit var pageCache: PdfCache
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        // 检查当前页面是否已经缓存
//        val currentPageBitmap = pageCache.getPage(currentPage)
//
//        if (currentPageBitmap != null) {
//            // 如果缓存中有该页面，直接从缓存中加载并绘制
//            canvas.drawBitmap(currentPageBitmap, 0f, 0f, null)
//        } else {
//            // 如果缓存中没有页面，渲染并缓存
//            val renderedBitmap = renderPdfPageWithPdfium(currentPageIndex)
//            pageCache.putPage(currentPage, renderedBitmap)
//            canvas.drawBitmap(renderedBitmap, 0f, 0f, null)
//        }
//    }
//}