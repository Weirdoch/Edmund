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
import com.example.edmund.data.parse.PdfCache
import com.example.edmund.databinding.ActivityReadBinding
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.shockwave.pdfium.PdfiumCore
import dagger.hilt.android.AndroidEntryPoint
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class ReadActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var binding: ActivityReadBinding
    private lateinit var inputStream: InputStream
    private lateinit var book: BookEntity
    private lateinit var pdfiumCore: PdfiumCore

    private val htmlContent = StringBuilder() // 存储加载的 HTML 内容
    private var currentPage = 0 // 当前加载到的章节索引

    private lateinit var uri: Uri


    private val pdfCache = PdfCache()



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
            // 加载 PDF 文件
//            pdfView = binding.pdfView
//            requestPermissions()
//            pdfParse.loadPdf(binding, inputStream, pdfCache, applicationContext, uri)

            binding.imageView.visibility = View.VISIBLE
            binding.webView.visibility = View.GONE
            renderPageAsBitmap(0)
        } else if (book.filePath.endsWith(".epub", ignoreCase = true)) {
            // 加载 EPUB 文件
            val loadedBook = loadEpub()
            loadedBook?.let {
                displayContinuousContentInWebView(binding.webView, it)
            }
        }
    }

    private fun renderPageAsBitmap(pageIndex: Int) {
        val fileDescriptorFromUri = getFileDescriptorFromUri(uri)
        val document = pdfiumCore.newDocument(fileDescriptorFromUri)
        Log.i("PDF", "PDF loaded, total pages: ${pdfiumCore.getPageCount(document)}")
        try {
            pdfiumCore.openPage(document, 0)

            val width = pdfiumCore.getPageWidth(document, 0)
            val height = pdfiumCore.getPageHeight(document, 0)

            // Create a Bitmap from the page
            var createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            pdfiumCore.renderPageBitmap(document, createBitmap, 0, 0, 0, width, height)

            // Optionally display the Bitmap in an ImageView
            binding.imageView.setImageBitmap(createBitmap)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFileDescriptorFromUri(uri: Uri): ParcelFileDescriptor {
        val resolver: ContentResolver = contentResolver
        return resolver.openFileDescriptor(uri, "r") ?: throw IOException("Unable to open file descriptor for Uri: $uri")
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
//
//    private fun loadPdf() {
//        binding.pdfView.visibility = View.VISIBLE
//        binding.webView.visibility = View.GONE
//
//        pdfView.fromStream(inputStream)
//            .enableSwipe(true)
//            .swipeHorizontal(false)
//            .enableDoubletap(true)
//            .defaultPage(0)
//            .onPageChange {
//                    page, _ ->
//                // 当前页面翻页时，缓存当前页和附近两页
//                cacheAdjacentPages(page, inputStream, pdfCache)
//
//            }
//            .onLoad { totalPages ->
//                Log.d("PDF", "PDF loaded, total pages: $totalPages")
//            }
//            .onError {
//                Log.e("PDF", "Error loading PDF")
//                Toast.makeText(this, "Error loading PDF", Toast.LENGTH_SHORT).show()
//            }
//            .onPageError { page, t ->
//                Log.e("PDF", "Error loading page $page", t)
//            }
//            .pageFitPolicy(FitPolicy.WIDTH)
//            .load()
//    }
//
//    fun loadPage(pageNumber: Int) {
//        // 检查内存缓存
//        var pageBitmap = pdfCache.getPage(pageNumber)
//
//
//        if (pageBitmap == null) {
//
//            //PdfRenderer.openPage(inputStream, pageNumber)
//            try {
//                val fileDescriptor = getParcelFileDescriptorFromUri(this, Uri.parse(book.filePath))
//                val renderer = PdfRenderer(fileDescriptor!!)
//                val page = renderer.openPage(pageNumber)
//                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
//                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//                page.close()
//                renderer.close()
//                pageBitmap = bitmap
//            } catch(e: Exception) {}
//
//            pageBitmap?.let {
//                pdfCache.putPage(pageNumber, it)
//            }
//        }
//    }
//
//    fun getParcelFileDescriptorFromUri(context: Context, uri: Uri): ParcelFileDescriptor? {
//        return try {
//            // 使用 ContentResolver 打开 Uri 并获取 ParcelFileDescriptor
//            context.contentResolver.openFileDescriptor(uri, "r")  // "r" 表示只读模式
//        } catch (e: IOException) {
//            e.printStackTrace()
//            null
//        }
//    }


    fun loadEpub(): Book? {
        binding.imageView.visibility = View.GONE
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
}
