package com.example.edmund.ui

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.edmund.Application
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.data.parse.PdfCacheManager
import com.example.edmund.databinding.ActivityReadBinding
import com.example.edmund.ui.adapter.PdfPageAdapter
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import dagger.hilt.android.AndroidEntryPoint
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.io.IOException
import java.io.InputStream
import kotlin.math.abs

class ReadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadBinding
    private lateinit var inputStream: InputStream
    private lateinit var book: BookEntity
    private lateinit var pdfiumCore: PdfiumCore
    private lateinit var pdfCacheManager: PdfCacheManager
    private var totalPageCount = 0
    private lateinit var pdfPageAdapter: PdfPageAdapter

    private val htmlContent = StringBuilder() // 存储加载的 HTML 内容

    private var currentPage = 0 // 当前加载到的章节索引
    private val pageStep = 2

    private lateinit var uri: Uri
    private lateinit var pdfDocument: PdfDocument

    private var width = 0
    private var height = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 获取传递过来的书籍信息
        book = intent.getSerializableExtra("book") as BookEntity
        uri = Uri.parse(book.filePath) // 获取传递的 content:// URI
        inputStream = getInputStreamFromUri(uri)


        // 判断文件类型并加载
        if (book.filePath.endsWith(".pdf", ignoreCase = true)) {
            binding.pdfView.visibility = View.VISIBLE
            binding.webView.visibility = View.GONE
            initializePdfReader()
            pdfPageAdapter = PdfPageAdapter(pdfCacheManager, totalPageCount)
            binding.pdfView.adapter = pdfPageAdapter
            //renderPages(currentPage)
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

    private fun initializePdfReader() {
        pdfiumCore = Application.pdfiumCore!!
        pdfDocument = pdfiumCore.newDocument(getFileDescriptorFromUri(uri))
        totalPageCount = pdfiumCore.getPageCount(pdfDocument)

        // 获取第一页的宽高信息
        pdfiumCore.openPage(pdfDocument, 0, 2)
        width = pdfiumCore.getPageWidth(pdfDocument, 0)
        height = pdfiumCore.getPageHeight(pdfDocument, 0)

        pdfCacheManager = PdfCacheManager(pdfiumCore, pdfDocument, totalPageCount)
    }



    // EPUB 相关操作
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


    //URI PARSE

    private fun getFileDescriptorFromUri(uri: Uri): ParcelFileDescriptor {
        val resolver: ContentResolver = contentResolver
        return resolver.openFileDescriptor(uri, "r") ?: throw IOException("Unable to open file descriptor for Uri: $uri")
    }

    private fun getInputStreamFromUri(uri: Uri): InputStream {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        return contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open input stream from URI")
    }
}
