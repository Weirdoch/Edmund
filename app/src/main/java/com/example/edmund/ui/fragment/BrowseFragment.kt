package com.example.edmund.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edmund.R
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.data.room.BookDao
import com.example.edmund.databinding.BrowseScreenBinding
import com.example.edmund.databinding.LibraryScreenBinding
import com.example.edmund.ui.adapter.LibraryAdapter
import java.io.File
import com.example.edmund.data.room.DatabaseHelper
import com.example.edmund.ui.adapter.BrowseAdapter
import java.util.Locale

class BrowseFragment : Fragment()  {

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        return inflater.inflate(R.layout.browse_screen, container, false)
//    }

    private lateinit var binding: BrowseScreenBinding
    private lateinit var bookDao: BookDao
    private lateinit var fileAdapter: BrowseAdapter
    private var selectedFiles = mutableListOf<File>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View  {

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
                // 获取文件夹路径
                val filePath = getRealPathFromURI(uri)
                loadFiles(filePath)
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        // 实现获取文件夹路径的逻辑
        return uri.path ?: ""
    }

    private fun loadFiles(path: String) {
        val directory = File(path)
        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles { _, name ->
                name.endsWith(".pdf", true) || name.endsWith(".epub", true)
            }
            fileAdapter.submitList(files?.toList())
        }
    }

    private fun onFileSelected(file: File) {
        if (selectedFiles.contains(file)) {
            selectedFiles.remove(file)
        } else {
            selectedFiles.add(file)
        }
    }

    // 导入文件到书架
    private suspend fun importFiles() {
        selectedFiles.forEach { file ->
            val book = BookEntity(
                title = file.nameWithoutExtension,
                author = null,
                description = "Imported from file system",
                filePath = file.absolutePath,
                scrollIndex = 0,
                scrollOffset = 0,
                progress = 0f,
                image = null,
                category = Locale.Category.FORMAT // 这里可以根据需要设置不同的分类
            )
            bookDao.insertBook(book)
        }
    }
}