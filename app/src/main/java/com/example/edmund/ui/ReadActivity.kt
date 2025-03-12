package com.example.edmund.ui

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

        pdfView = binding.pdfView

        //读写权限
        requestPermissions()

        // 获取传递过来的书籍信息
        book = intent.getSerializableExtra("book") as BookEntity

        val pdfUri: Uri = Uri.parse(book.filePath)  // 获取传递的 content:// URI

        // 使用 ContentResolver 获取文件输入流
        pdfInputStream = getInputStreamFromUri(pdfUri)

        // 加载 PDF 文件
        loadPdf()
    }

    //读写权限
    private fun requestPermissions() {
        // 检查权限是否已授予
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // 已经授予权限，可以进行读写操作
            return
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) ActivityCompat.requestPermissions(
            this, arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE), 0
        )

    }


    private fun getInputStreamFromUri(uri: Uri): InputStream {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        return contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open input stream from URI")
    }

    private fun loadPdf() {

        pdfView.fromStream(pdfInputStream).enableSwipe(true).swipeHorizontal(false)
            .enableDoubletap(true).defaultPage(0).onPageChange { page, _ ->
                currentPage = page
            }.onLoad { totalPages ->
                Log.d("PDF", "PDF loaded, total pages: $totalPages")
            }.onError {
                Log.e("PDF", "Error loading PDF")
                Toast.makeText(this, "Error loading PDF", Toast.LENGTH_SHORT).show()
            }.onPageError { page, t ->
                Log.e("PDF", "Error loading page $page", t)
            }.pageFitPolicy(FitPolicy.WIDTH) // 根据屏幕宽度适配
            .load()
    }


}


