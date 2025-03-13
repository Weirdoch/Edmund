package com.example.edmund.ui

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.databinding.ActivityReadBinding
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resources
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.InputStream

class ReadActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView

    private lateinit var book: BookEntity
    private lateinit var binding: ActivityReadBinding
    private lateinit var pdfInputStream: InputStream
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

        // 判断文件类型并加载
        if (book.filePath.endsWith(".pdf", ignoreCase = true)) {
            // 加载 PDF 文件
            pdfView = binding.pdfView
            requestPermissions()
            val pdfUri: Uri = Uri.parse(book.filePath) // 获取传递的 content:// URI
            pdfInputStream = getInputStreamFromUri(pdfUri)
            loadPdf()
        } else if (book.filePath.endsWith(".epub", ignoreCase = true)) {
            // 加载 EPUB 文件
            loadEpub()
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
        binding.epubTextView.visibility = View.GONE

        pdfView.fromStream(pdfInputStream)
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

    private fun loadEpub() {

        binding.pdfView.visibility = View.GONE
        binding.epubTextView.visibility = View.VISIBLE

        try {
            // 使用 EpubReader 解析 EPUB 文件
            val epubUri: Uri = Uri.parse(book.filePath)
            val epubInputStream: InputStream = getInputStreamFromUri(epubUri)

            // 读取 EPUB 文件
            val book: Book = EpubReader().readEpub(epubInputStream)

            // 获取书籍标题
            val title = book.title
            Log.d("EPUB", "Book Title: $title")

            val spine = book.spine
            val firstResource = spine.getResource(0)


            // 显示章节内容（例如在 TextView 中显示）
            binding.epubTextView.text = firstResource.reader.readText()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading EPUB", Toast.LENGTH_SHORT).show()
        }
    }


}
