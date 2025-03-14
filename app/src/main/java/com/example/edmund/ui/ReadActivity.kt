package com.example.edmund.ui

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
    private lateinit var binding: ActivityReadBinding
    private lateinit var inputStream: InputStream
    private var currentPage = 0
    private lateinit var book: BookEntity

    private var htmlContent = StringBuilder() // 存储加载的 HTML 内容

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
            val loadedBook = loadEpub()
            loadedBook?.let {
                displayContinuousContentInWebView(binding.webView, it)
            }
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

    // 以连续的方式加载 EPUB 内容
    fun displayContinuousContentInWebView(webView: WebView, book: Book) {
        val toc = book.tableOfContents.tocReferences

        // 遍历 EPUB 目录，合并所有内容为一个大的 HTML 页面
        if (toc.isNotEmpty()) {
            for (chapter in toc) {
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

        // 这里可以根据实际需要加载更多内容，当前示例为加载后续章节
        if (toc.isNotEmpty()) {
            for (index in currentPage until toc.size) {
                val chapter = toc[index]
                val resource = chapter.resource
                val chapterHtml = String(resource.data)
                htmlContent.append(chapterHtml) // 合并章节内容
            }

            // 将新的内容加载到 WebView 中
            webView.loadDataWithBaseURL("", htmlContent.toString(), "text/html", "UTF-8", null)
            currentPage = toc.size // 更新章节索引
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
}
