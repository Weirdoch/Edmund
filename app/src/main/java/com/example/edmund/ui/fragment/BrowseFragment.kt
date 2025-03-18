package com.example.edmund.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edmund.Application
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.data.parse.EpubParse.getBookInfo
import com.example.edmund.data.parse.EpubParse.getCoverImage
import com.example.edmund.data.parse.EpubParse.parseEpub
import com.example.edmund.data.parse.PdfParser
import com.example.edmund.data.room.BookDao
import com.example.edmund.databinding.BrowseScreenBinding
import com.example.edmund.data.room.DatabaseHelper
import com.example.edmund.domain.library.Category
import com.example.edmund.domain.use_case.permission.GrantPersistableUriPermission
import com.example.edmund.ui.adapter.BrowseAdapter
import com.shockwave.pdfium.PdfiumCore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class BrowseFragment : Fragment() {

    private lateinit var binding: BrowseScreenBinding
    private lateinit var bookDao: BookDao
    private lateinit var fileAdapter: BrowseAdapter
    private var selectedFiles = mutableListOf<DocumentFile>()
    @Inject lateinit var grantPersistableUriPermission: GrantPersistableUriPermission

    private lateinit var  pdfParser: PdfParser

    // 使用 SharedPreferences 保存路径
    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    private val folderUriKey = "saved_folder_uri"  // SharedPreferences 的 key

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BrowseScreenBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // 初始化 RecyclerView 和适配器
        fileAdapter = BrowseAdapter { file -> onFileSelected(file) }
        binding.browseList.layoutManager = LinearLayoutManager(context)
        binding.browseList.adapter = fileAdapter

        // 获取数据库实例
        val db = context?.let { DatabaseHelper.getDatabase(it) }
        if (db != null) {
            bookDao = db.bookDao
        }

        // 初始化 PDF 解析器
        pdfParser = PdfParser(requireContext(), Application.pdfiumCore!!)

        // 打开文件选择器
        binding.browseButton.setOnClickListener {
            openFolderPicker()
        }

        // 设置导入按钮点击事件
        binding.importButton.setOnClickListener {
            importFiles()
        }

        // 检查是否有已保存的文件夹路径
        CoroutineScope(Dispatchers.IO).launch{
            val savedFolderUri = sharedPreferences.getString(folderUriKey, null)
            if (savedFolderUri != null) {
                // 如果有，直接打开该文件夹
                loadFiles(Uri.parse(savedFolderUri))
            }
        }

        return rootView
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        CoroutineScope(Dispatchers.IO).launch{
            if (resultCode == RESULT_OK && requestCode == 1) {
                data?.data?.also { uri ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            grantPersistableUriPermission.execute(uri)
                        } catch (e: Exception) {
                            // 处理异常
                            Log.e(TAG, "Permission grant failed", e)
                        }
                    }
                    // 保存选中的文件夹路径到 SharedPreferences
                    saveFolderUri(uri)
                    // 获取文件夹路径
                    loadFiles(uri)
                }
            }
        }
    }

    private fun saveFolderUri(uri: Uri) {
        // 保存文件夹 URI 到 SharedPreferences
        sharedPreferences.edit().putString(folderUriKey, uri.toString()).apply()
    }

    private suspend fun loadFiles(uri: Uri) {
        val documentFile = context?.let { DocumentFile.fromTreeUri(it, uri) }

        if (documentFile != null) {
            val files = documentFile.listFiles()
            //去除已导入书籍
            val allBooks = bookDao.getAllBooks()
            val hadfiles = allBooks.map { book -> book.filePath }
            files.filter { if (hadfiles.contains(it.uri.toString())) { false } else { true } }
            val pdfFiles = files.filter { it.name?.endsWith(".pdf", true) == true }
            val epubFiles = files.filter { it.name?.endsWith(".epub", true) == true }
            withContext(Dispatchers.Main){
                fileAdapter.submitList(pdfFiles + epubFiles) // 显示 PDF 和 EPUB 文件
            }
        }
    }

    // 处理文件选择
    private fun onFileSelected(file: DocumentFile) {
        if (selectedFiles.contains(file)) {
            selectedFiles.remove(file)
        } else {
            selectedFiles.add(file)
        }
        updateImportButtonVisibility() // 更新导入按钮的显示状态
    }

    // 更新导入按钮的显示
    private fun updateImportButtonVisibility() {
        if (selectedFiles.isNotEmpty()) {
            binding.importButton.visibility = View.VISIBLE // 显示导入按钮
        } else {
            binding.importButton.visibility = View.GONE // 隐藏导入按钮
        }
    }

    // 导入选中的文件
    private fun importFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            selectedFiles.forEach { file ->
                when {
                    file.name?.endsWith(".pdf", true) == true -> {
                        // 处理 PDF 文件
                        pdfInsert(file)
                    }
                    file.name?.endsWith(".epub", true) == true -> {
                        // 处理 EPUB 文件
                        epubInsert(file)
                    }
                }
            }

            // 完成后清空选中文件并更新 UI
            withContext(Dispatchers.Main) {
                //提示导入成功
                Toast.makeText(context, "Imported ${selectedFiles.size} files", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun pdfInsert(file: DocumentFile) {
        val filePath = file.uri.toString() // 获取文件 URI
        val metadata = pdfParser.extractMetadata(file.uri)

        var fileNameWithoutExtension = metadata["Title"]

        if (metadata["Title"] == null || metadata["Title"] == "") {
            fileNameWithoutExtension = file.name?.let {
                it.substringBeforeLast(".", it)
            } ?: "Unknown Title" // 默认文件名
        }

        var author = metadata["Author"]
        if (metadata["Author"] == null || metadata["Author"] == "") {
            author = "Unknown Author" // 默认作者
        }

        // 创建 BookEntity 实例
        val book = BookEntity(
            title = fileNameWithoutExtension ?: "Unknown Title",
            author = author, // 可以根据需求提取作者
            description = "Imported from file system", // 描述信息
            filePath = filePath,
            scrollIndex = 0,
            scrollOffset = 0,
            progress = 0f,
            image = null, // 可选封面图
            category = Category.READING // 默认分类
        )

        // 插入到数据库
        bookDao.insertBook(book)
    }

    private suspend fun epubInsert(file: DocumentFile) {
        val filePath = file.uri.toString() // 获取文件 URI
        val parseEpub = parseEpub(getInputStreamFromUri(file.uri))
        if (parseEpub.isFailure) {
            // 处理解析失败的情况
            return
        }
        val metadata = getBookInfo(parseEpub.getOrThrow())

        var fileNameWithoutExtension = metadata["Title"]

        if (metadata["Title"] == null || metadata["Title"] == "") {
            fileNameWithoutExtension = file.name?.let {
                it.substringBeforeLast(".", it)
            } ?: "Unknown Title" // 默认文件名
        }

        var author = metadata["Author"]
        if (metadata["Author"] == null || metadata["Author"] == "") {
            author = "Unknown Author" // 默认作者
        }

        // 创建 BookEntity 实例
        val book = BookEntity(
            title = fileNameWithoutExtension ?: "Unknown Title",
            author = author, // 可以根据需求提取作者
            description = metadata["Description"], // 描述信息
            filePath = filePath,
            scrollIndex = 0,
            scrollOffset = 0,
            progress = 0f,
            image = null, // 可选封面图
            category = Category.READING // 默认分类
        )

        // 插入到数据库
        bookDao.insertBook(book)
    }


    private fun getInputStreamFromUri(uri: Uri): InputStream {
        val contentResolver: ContentResolver = requireContext().contentResolver
        return contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open input stream from URI")
    }

}
