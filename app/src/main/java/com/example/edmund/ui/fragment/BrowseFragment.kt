package com.example.edmund.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.data.room.BookDao
import com.example.edmund.databinding.BrowseScreenBinding
import java.io.File
import com.example.edmund.data.room.DatabaseHelper
import com.example.edmund.ui.adapter.BrowseAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class BrowseFragment : Fragment() {

    private lateinit var binding: BrowseScreenBinding
    private lateinit var bookDao: BookDao
    private lateinit var fileAdapter: BrowseAdapter
    private var selectedFiles = mutableListOf<DocumentFile>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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

        // 打开文件选择器
        binding.browseButton.setOnClickListener {
            openFolderPicker()
        }

        return rootView
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            data?.data?.also { uri ->
                loadFiles(uri)
            }
        }
    }

    private fun loadFiles(uri: Uri) {
        val documentFile = context?.let { DocumentFile.fromTreeUri(it, uri) }
        if (documentFile != null) {
            val files = documentFile.listFiles()
            val pdfFiles = files.filter { it.name?.endsWith(".pdf", true) == true }
            val epubFiles = files.filter { it.name?.endsWith(".epub", true) == true }
            // 合并所有符合条件的文件
            fileAdapter.submitList(pdfFiles + epubFiles)
        }
    }


    private fun onFileSelected(file: DocumentFile) {
        if (selectedFiles.contains(file)) {
            selectedFiles.remove(file)
        } else {
            selectedFiles.add(file)
        }
        updateImportButtonVisibility()  // 更新导入按钮的可见性
    }

    private fun updateImportButtonVisibility() {
        if (selectedFiles.isNotEmpty()) {
            binding.importButton.visibility = View.VISIBLE  // 显示导入按钮
        } else {
            binding.importButton.visibility = View.GONE  // 隐藏导入按钮
        }
    }




    // 导入文件到书架
    private fun importFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            selectedFiles.forEach { file ->
                // 获取文件名并去掉扩展名
                val fileNameWithoutExtension = file.name?.let {
                    it.substringBeforeLast(".", it) // 去掉扩展名
                } ?: "Unknown Title" // 如果文件没有扩展名，默认使用 "Unknown Title"

                // 获取文件路径（通过 URI 访问）
                val filePath = file.uri.toString() // 使用 URI 而不是直接的路径

                val book = BookEntity(
                    title = fileNameWithoutExtension,
                    author = null, // 这里你可以根据需要提取作者信息
                    description = "Imported from file system", // 简单描述
                    filePath = filePath,
                    scrollIndex = 0,
                    scrollOffset = 0,
                    progress = 0f,
                    image = null, // 可以根据文件选择封面图像
                    category = Locale.Category.FORMAT // 可以根据需求调整分类
                )
                // 插入到数据库
                bookDao.insertBook(book)
            }
        }
    }

}
