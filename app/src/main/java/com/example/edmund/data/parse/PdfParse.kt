package com.example.edmund.data.parse

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.databinding.ActivityReadBinding
import com.example.edmund.ui.ReadActivity
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject


//class PdfParse @Inject constructor() {
//
//
//    private var currentPage = 2
//
//
//    fun loadPdf(binding: ActivityReadBinding, inputStream: InputStream, pdfCache: PdfCache, applicationContext: Context, uri: Uri) {
//        binding.pdfView.visibility = View.VISIBLE
//        binding.webView.visibility = View.GONE
//
//        //预加载设置
//
//        binding.pdfView.fromStream(inputStream)
//            .enableSwipe(true)
//            .swipeHorizontal(true)
//            .enableDoubletap(true)
//            .defaultPage(0)
//            .onPageChange { page, pageCount ->
//                Log.d("PDF", "Current page: $page, total pages: $pageCount")
//                currentPage = page
//                // 缓存相邻页面
//                cacheAdjacentPages(currentPage, inputStream, pdfCache, applicationContext, uri)
//            }
//            .onLoad { totalPages ->
//                Log.d("PDF", "PDF loaded, total pages: $totalPages")
//            }
//            .onError {
//                Log.e("PDF", "Error loading PDF")
//                Toast.makeText(applicationContext, "Error loading PDF", Toast.LENGTH_SHORT).show()
//            }
//            .onPageError { page, t ->
//                Log.e("PDF", "Error loading page $page", t)
//            }
//            .pageFitPolicy(FitPolicy.WIDTH)
//            .onPageScroll { page, positionOffset ->  }
//            .load()
//    }
//
//    fun cacheAdjacentPages(
//        currentPage: Int,
//        inputStream: InputStream,
//        cache: PdfCache,
//        applicationContext: Context,
//        uri: Uri
//    ) {
//        // 缓存当前页和后两页
//        for (pageIndex in currentPage..currentPage + 5) {
//            if (pageIndex >= 0) {
//                val cachedBitmap = cache.getPage(pageIndex)
//                if (cachedBitmap == null) {
//                    // 渲染该页面并缓存
//                    val renderedBitmap = renderPageToBitmap(inputStream, pageIndex, applicationContext, uri)
//                    if (renderedBitmap != null) {
//                        cache.putPage(pageIndex, renderedBitmap)
//                    }
//                }
//            }
//        }
//    }
//
//    fun renderPageToBitmap(inputStream: InputStream, pageIndex: Int, applicationContext: Context, uri: Uri): Bitmap? {
//        // 使用 PdfRenderer 或 PdfView 渲染页面为 Bitmap
//        if (pageIndex < 0) {
//            return null
//        }
//        try {
//            val fileDescriptor = getParcelFileDescriptorFromUri(applicationContext, uri)
//            val renderer = PdfRenderer(fileDescriptor!!)
//            val page = renderer.openPage(pageIndex)
//
//            // 创建 Bitmap 并渲染页面
//            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
//            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//            page.close()
//            renderer.close()
//            return bitmap
//        } catch (e: Exception) {
//            Log.e("PDF", "Error rendering page $pageIndex", e)
//            return null
//        }
//    }
//
//    // 获取 ParcelFileDescriptor
//    fun getParcelFileDescriptorFromUri(context: Context, uri: Uri): ParcelFileDescriptor? {
//        return try {
//            context.contentResolver.openFileDescriptor(uri, "r") // "r" 表示只读模式
//        } catch (e: IOException) {
//            Log.e("PDF", "Error opening file descriptor for URI: $uri", e)
//            null
//        }
//    }
//}
