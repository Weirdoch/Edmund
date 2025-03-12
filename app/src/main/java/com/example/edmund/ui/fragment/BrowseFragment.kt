package com.example.edmund.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.data.room.BookDao
import com.example.edmund.databinding.BrowseScreenBinding
import com.example.edmund.data.room.DatabaseHelper
import com.example.edmund.domain.library.Category
import com.example.edmund.domain.use_case.permission.GrantPersistableUriPermission
import com.example.edmund.ui.adapter.BrowseAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class BrowseFragment : Fragment() {

    private lateinit var binding: BrowseScreenBinding
    private lateinit var bookDao: BookDao
    private lateinit var fileAdapter: BrowseAdapter
    private var selectedFiles = mutableListOf<DocumentFile>()
    @Inject lateinit var grantPersistableUriPermission: GrantPersistableUriPermission

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

        // 打开文件选择器
        binding.browseButton.setOnClickListener {
            openFolderPicker()
        }

        // 设置导入按钮点击事件
        binding.importButton.setOnClickListener {
            importFiles()
        }

        // 检查是否有已保存的文件夹路径
        val savedFolderUri = sharedPreferences.getString(folderUriKey, null)
        if (savedFolderUri != null) {
            // 如果有，直接打开该文件夹
            loadFiles(Uri.parse(savedFolderUri))
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

    private fun saveFolderUri(uri: Uri) {
        // 保存文件夹 URI 到 SharedPreferences
        sharedPreferences.edit().putString(folderUriKey, uri.toString()).apply()
    }

    private fun loadFiles(uri: Uri) {
        val documentFile = context?.let { DocumentFile.fromTreeUri(it, uri) }

        if (documentFile != null) {
            val files = documentFile.listFiles()
            val pdfFiles = files.filter { it.name?.endsWith(".pdf", true) == true }
            val epubFiles = files.filter { it.name?.endsWith(".epub", true) == true }
            fileAdapter.submitList(pdfFiles + epubFiles) // 显示 PDF 和 EPUB 文件
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
                val fileNameWithoutExtension = file.name?.let {
                    it.substringBeforeLast(".", it)
                } ?: "Unknown Title" // 默认文件名

                val filePath = file.uri.toString() // 获取文件 URI

                // 创建 BookEntity 实例
                val book = BookEntity(
                    title = fileNameWithoutExtension,
                    author = null, // 可以根据需求提取作者
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

            // 完成后清空选中文件并更新 UI
            withContext(Dispatchers.Main) {
                selectedFiles.clear()
                updateImportButtonVisibility() // 隐藏导入按钮
            }
        }
    }
}
