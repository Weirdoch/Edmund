package com.example.edmund.ui

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.data.parse.EpubParse
import com.example.edmund.databinding.ActivityReadBinding
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.io.InputStream

class ReadActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView

    private lateinit var book: BookEntity
    private lateinit var binding: ActivityReadBinding
    private lateinit var inputStream: InputStream
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 获取传递过来的书籍信息
        book = intent.getSerializableExtra("book") as BookEntity

        val uri: Uri = Uri.parse(book.filePath) // 获取传递的 content:// URI
        inputStream = getInputStreamFromUri(uri)

        // 判断文件类型并加载
        if (book.filePath.endsWith(".pdf", ignoreCase = true)) {
            // 加载 PDF 文件
            pdfView = binding.pdfView
            requestPermissions()

            loadPdf()
        } else if (book.filePath.endsWith(".epub", ignoreCase = true)) {
            // 加载 EPUB 文件
            val book = loadEpub()
            book?.let { displayFirstChapterInWebView(binding.webView, it) }
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

    private fun getInputStreamFromUri(uri: Uri): InputStream {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        return contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open input stream from URI")
    }

    private fun loadPdf() {

        binding.pdfView.visibility = View.VISIBLE
        binding.webView.visibility = View.GONE

        pdfView.fromStream(inputStream)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .onPageChange { page, _ -> currentPage = page }
            .onLoad { totalPages ->
                Log.d("PDF", "PDF loaded, total pages: $totalPages")
            }
            .onError {
                Log.e("PDF", "Error loading PDF")
                Toast.makeText(this, "Error loading PDF", Toast.LENGTH_SHORT).show()
            }
            .onPageError { page, t ->
                Log.e("PDF", "Error loading page $page", t)
            }
            .pageFitPolicy(FitPolicy.WIDTH)
            .load()
    }

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

    fun displayFirstChapterInWebView(webView: WebView, book: Book) {
        book.contents.forEach {
            Log.d("Epub", "Content: ${it.title}")
        }
        val toc = book.tableOfContents.tocReferences
        if (toc.isNotEmpty()) {
//            val firstChapter = toc[5]
//            val href = firstChapter.resource.href
            val htmlContent = EpubParse.getChapterHtml(book)
            htmlContent?.let { webView.loadData(it, "text/html", "UTF-8") }
        }
    }

    // 翻页功能
//    fun nextPage() {
//        val htmlContent = getNextChapter(book)
//        webView.loadData(htmlContent, "text/html", "UTF-8")
//    }
//
//    fun previousPage() {
//        val htmlContent = getPreviousChapter(book)
//        webView.loadData(htmlContent, "text/html", "UTF-8")
//    }
//}


}
