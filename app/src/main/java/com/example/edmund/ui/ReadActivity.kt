package com.example.edmund.ui

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.data.parse.EpubParse
import com.example.edmund.data.parse.PdfCache
import com.example.edmund.databinding.ActivityReadBinding
import com.github.barteksc.pdfviewer.PDFView
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import dagger.hilt.android.AndroidEntryPoint
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.io.IOException
import java.io.InputStream
import kotlin.math.abs

@AndroidEntryPoint
class ReadActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var binding: ActivityReadBinding
    private lateinit var inputStream: InputStream
    private lateinit var book: BookEntity
    private lateinit var pdfiumCore: PdfiumCore
    private val pdfCache = PdfCache()

    private val htmlContent = StringBuilder() // 存储加载的 HTML 内容

    private var currentPage = 0 // 当前加载到的章节索引
    private val pageStep = 2

    private lateinit var uri: Uri
    private lateinit var pdfDocument: PdfDocument


    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(this, GestureListener())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 设置边缘到边缘适配
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 获取传递过来的书籍信息
        book = intent.getSerializableExtra("book") as BookEntity
        uri = Uri.parse(book.filePath) // 获取传递的 content:// URI
        inputStream = getInputStreamFromUri(uri)


        pdfiumCore = PdfiumCore(this)


        // 判断文件类型并加载
        if (book.filePath.endsWith(".pdf", ignoreCase = true)) {
            binding.pdfView.visibility = View.VISIBLE
            binding.webView.visibility = View.GONE
            pdfDocument = pdfiumCore.newDocument(getFileDescriptorFromUri(uri))
            renderPages(currentPage)
        } else if (book.filePath.endsWith(".epub", ignoreCase = true)) {
            // 加载 EPUB 文件
            val loadedBook = loadEpub()
            loadedBook?.let {
                displayContinuousContentInWebView(binding.webView, it)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            // 关闭 PDF 文档
            pdfiumCore.closeDocument(pdfDocument)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 让 GestureDetector 处理触摸事件
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    // 通过手势检测进行翻页
    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // 判断左右滑动的手势
            if (e1 != null) {
                if (abs(e2.x - e1.x) > 100 && abs(velocityX) > 1000) {
                    // 如果是右滑动 (前一页)
                    if (e2.x > e1.x) {
                        previousPage()
                    }
                    // 如果是左滑动 (下一页)
                    else {
                        nextPage()
                    }
                    return true
                }
            }
            return false
        }

    }

    // 渲染当前页和下一页
    private fun renderPages(pageIndex: Int) {
        val imageView1 = binding.imageView1
        val imageView2 = binding.imageView2
        pdfiumCore.openPage(pdfDocument, pageIndex)
        val width = pdfiumCore.getPageWidth(pdfDocument, pageIndex)
        val height = pdfiumCore.getPageHeight(pdfDocument, pageIndex)
        try {
            // 获取并渲染第一页
            var bitmap1 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap1, pageIndex, 0, 0, width, height)
            imageView1.setImageBitmap(bitmap1)

            // 获取并渲染第二页
            if (pageIndex + 1 < pdfiumCore.getPageCount(pdfDocument)) {
                pdfiumCore.openPage(pdfDocument, pageIndex + 1)
                var bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                pdfiumCore.renderPageBitmap(pdfDocument, bitmap2, pageIndex + 1, 0, 0, width, height)
                imageView2.setImageBitmap(bitmap2)
            } else {
                imageView2.setImageBitmap(null) // 如果没有第二页，清除 ImageView
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 显示上一页
    private fun previousPage() {
        if (currentPage > 0) {
            currentPage -= pageStep
            if (currentPage < 0) currentPage = 0
            renderPages(currentPage)
        }
    }

    // 显示下一页
    private fun nextPage() {
        if (currentPage + pageStep < pdfiumCore.getPageCount(pdfDocument)) {
            currentPage += pageStep
            renderPages(currentPage)
        }
    }


    private fun getFileDescriptorFromUri(uri: Uri): ParcelFileDescriptor {
        val resolver: ContentResolver = contentResolver
        return resolver.openFileDescriptor(uri, "r") ?: throw IOException("Unable to open file descriptor for Uri: $uri")
    }


    private fun getInputStreamFromUri(uri: Uri): InputStream {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        return contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open input stream from URI")
    }






    //epub


    fun loadEpub(): Book? {
        binding.pdfView.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE

        return try {
            EpubReader().readEpub(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 以连续的方式加载 EPUB 内容
    fun displayContinuousContentInWebView(webView: WebView, book: Book) {
        val toc = book.tableOfContents.tocReferences

        // 遍历 EPUB 目录，合并所有内容为一个大的 HTML 页面
        if (toc.isNotEmpty()) {
            // 加载前 3 页内容，避免一次性加载所有章节
            val chaptersToLoad = toc.take(3)
            for (chapter in chaptersToLoad) {
                val resource = chapter.resource
                val chapterHtml = String(resource.data)
                htmlContent.append(chapterHtml) // 合并章节内容
            }

            // 加载所有内容到 WebView
            webView.loadDataWithBaseURL("", htmlContent.toString(), "text/html", "UTF-8", null)

            // 监听滚动事件，加载更多内容
            webView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                // 如果滚动到底部，加载更多内容
                if (scrollY + webView.height >= webView.contentHeight * webView.scale) {
                    loadMoreContent(webView, book)
                }
            }
        }
    }

    // 加载更多内容
    private fun loadMoreContent(webView: WebView, book: Book) {
        val toc = book.tableOfContents.tocReferences

        // 如果当前章节索引未到达最后，继续加载后续章节
        if (currentPage < toc.size) {
            val chaptersToLoad = toc.subList(currentPage, minOf(currentPage + 3, toc.size))
            for (chapter in chaptersToLoad) {
                val resource = chapter.resource
                val chapterHtml = String(resource.data)
                htmlContent.append(chapterHtml) // 合并章节内容
            }

            // 将新的内容加载到 WebView 中
            webView.loadDataWithBaseURL("", htmlContent.toString(), "text/html", "UTF-8", null)
            currentPage += chaptersToLoad.size // 更新章节索引
        }
    }

    // 额外的方法：使 WebView 不被遮挡
    private fun enableEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }


    // 读写权限
    private fun requestPermissions() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0
            )
        }
    }
}
