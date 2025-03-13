package com.example.edmund.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.data.room.BookDao
import com.example.edmund.data.room.DatabaseHelper
import com.example.edmund.databinding.LibraryScreenBinding
import com.example.edmund.domain.library.Category
import com.example.edmund.ui.ReadActivity
import com.example.edmund.ui.adapter.LibraryAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment() {

    private lateinit var binding: LibraryScreenBinding
    private lateinit var recyclerViewAdapter: LibraryAdapter
    private lateinit var bookDao: BookDao
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val bookCategories = mutableMapOf<Category, MutableList<BookEntity>>()  // 使用map存储不同类别的书籍

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LibraryScreenBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // 初始化数据库和RecyclerView
        val db = context?.let { DatabaseHelper.getDatabase(it) }
        db?.let { bookDao = it.bookDao }
        initRecyclerView()

        // 加载并更新书籍
        loadBooksFromDatabase()

        // 设置TabLayout和下拉刷新
        setupTabLayout()
        setupSwipeRefreshLayout()

        return rootView
    }

    private fun initRecyclerView() {
        recyclerViewAdapter = LibraryAdapter().apply {
            setOnItemClickListener { book -> startReadActivity(book) }
            setOnItemLongClickListener { book, position -> showItemOptionsDialog(book, position) }
        }
        binding.libraryList.apply {
            adapter = recyclerViewAdapter
            layoutManager = GridLayoutManager(context, 5)
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { updateRecyclerViewByTab(it.position) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            loadBooksFromDatabase()
            updateRecyclerViewByTab(binding.tabLayout.selectedTabPosition)
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadBooksFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            bookCategories.clear()  // 清空之前的书籍数据
            val books = bookDao.getAllBooks()
            books.forEach { book ->
                bookCategories.getOrPut(book.category) { mutableListOf() }.add(book)
            }
            withContext(Dispatchers.Main) {
                updateRecyclerViewByTab(binding.tabLayout.selectedTabPosition)  // 切换选中的tab更新RecyclerView
            }
        }
    }

    private fun updateRecyclerViewByTab(tabPosition: Int) {
        val selectedCategory = Category.values()[tabPosition]  // 根据tab的位置选择对应的书籍类别
        val books = bookCategories[selectedCategory] ?: emptyList()
        recyclerViewAdapter.submitList(books)
    }

    private fun startReadActivity(book: BookEntity) {
        val intent = Intent(requireContext(), ReadActivity::class.java).apply {
            putExtra("book", book)
        }
        startActivity(intent)
    }

    private fun showItemOptionsDialog(book: BookEntity, position: Int) {
        val options = arrayOf("移动", "删除")
        AlertDialog.Builder(requireContext())
            .setItems(options) { _, which ->
                when (which) {
                    0 -> moveBook(book, position)
                    1 -> deleteBook(book, position)
                }
            }.show()
    }

    private fun deleteBook(book: BookEntity, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            bookDao.deleteBook(book)  // 从数据库删除书籍
            withContext(Dispatchers.Main) {
                recyclerViewAdapter.removeBook(position)  // 更新UI
                Toast.makeText(requireContext(), "书籍已删除", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveBook(book: BookEntity, position: Int) {
        val newCategory = Category.ALREADY_READ  // 将书籍移动到已读分类
        book.category = newCategory
        CoroutineScope(Dispatchers.IO).launch {
            bookDao.updateBook(book)  // 更新数据库中的书籍
            withContext(Dispatchers.Main) {
                recyclerViewAdapter.removeBook(position)  // 更新UI
                Toast.makeText(requireContext(), "书籍已移动到 $newCategory", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
