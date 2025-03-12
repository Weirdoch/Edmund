package com.example.edmund.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookDao: BookDao
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val readingBooks = mutableListOf<BookEntity>()  // 正在阅读的书籍
    private val alreadyReadBooks = mutableListOf<BookEntity>()  // 已读书籍
    private val planningBooks = mutableListOf<BookEntity>()  // 计划中的书籍
    private val droppedBooks = mutableListOf<BookEntity>()  // 丢弃的书籍

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 绑定布局文件
        binding = LibraryScreenBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // 获取数据库实例
        val db = context?.let { DatabaseHelper.getDatabase(it) }
        if (db != null) {
            bookDao = db.bookDao
        }

        // 初始化 RecyclerView
        recyclerView = binding.libraryList
        recyclerViewAdapter = LibraryAdapter()
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 5, GridLayoutManager.VERTICAL, false)

        swipeRefreshLayout = binding.swipeRefreshLayout

        // 从数据库加载数据
        loadBooksFromDatabase()

        // 设置点击事件监听
        // 设置点击事件监听
        recyclerViewAdapter.setOnItemClickListener { book ->
            // 跳转到 PDF 阅读 Activity
            val intent = Intent(requireContext(), ReadActivity::class.java)
            intent.putExtra("book", book)  // 传递书籍对象
            startActivity(intent)
        }

        // 设置 TabLayout 的监听器
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> updateRecyclerView(readingBooks)  // 阅读中的书籍
                        1 -> updateRecyclerView(alreadyReadBooks)  // 已读书籍
                        2 -> updateRecyclerView(planningBooks)  // 计划中的书籍
                        3 -> updateRecyclerView(droppedBooks)  // 丢弃的书籍
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // 可以在此做一些清理操作，暂时不做处理
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // 当重新选择已经选中的 Tab 时，可以不做任何操作
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            // 执行刷新操作
            loadBooksFromDatabase()  // 刷新数据
        }

        return rootView
    }

    private fun loadBooksFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            // 从数据库中获取各状态的书籍
            readingBooks.clear()
            alreadyReadBooks.clear()
            planningBooks.clear()
            droppedBooks.clear()

            val books = bookDao.getAllBooks()  // 获取所有书籍
            for (book in books) {
                when (book.category) {
                    Category.READING -> readingBooks.add(book)
                    Category.ALREADY_READ -> alreadyReadBooks.add(book)
                    Category.PLANNING -> planningBooks.add(book)
                    Category.DROPPED -> droppedBooks.add(book)
                }
            }

            // 更新 UI
            withContext(Dispatchers.Main) {
                //根据tab展示对应书籍列表
                when (binding.tabLayout.selectedTabPosition) {
                    0 -> updateRecyclerView(readingBooks)  // 阅读中的书籍
                    1 -> updateRecyclerView(alreadyReadBooks)  // 已读书籍
                    2 -> updateRecyclerView(planningBooks)  // 计划中的书籍
                    3 -> updateRecyclerView(droppedBooks)  // 丢弃的书籍
                }
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun updateRecyclerView(data: List<BookEntity>) {
        // 更新 RecyclerView 的数据
        recyclerViewAdapter.submitList(data)
    }
}
