package com.example.edmund.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edmund.databinding.LibraryScreenBinding
import com.example.edmund.ui.adapter.LibraryAdapter
import com.google.android.material.tabs.TabLayout

class LibraryFragment : Fragment() {

    private lateinit var binding: LibraryScreenBinding
    private lateinit var recyclerViewAdapter: LibraryAdapter
    private lateinit var recyclerView: RecyclerView
    private val readingBooks = mutableListOf<String>()  // 假设的阅读中的书籍
    private val alreadyReadBooks = mutableListOf<String>()  // 已读书籍
    private val planningBooks = mutableListOf<String>()  // 计划中的书籍
    private val droppedBooks = mutableListOf<String>()  // 丢弃的书籍

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 绑定布局文件
        binding = LibraryScreenBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // 初始化 RecyclerView
        recyclerView = binding.libraryList
        recyclerViewAdapter = LibraryAdapter()
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 5, GridLayoutManager.VERTICAL, false)

        // 填充一些数据（这些数据你可以替换为从数据库或网络请求中获取的数据）
        populateData()
        updateRecyclerView(readingBooks)

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

        return rootView
    }

    private fun populateData() {
        // 添加一些假数据
        readingBooks.add("Book 1 - Reading")
        readingBooks.add("Book 1.1 - Reading")
        readingBooks.add("Book 1.2 - Reading")
        readingBooks.add("Book 1.3 - Reading")
    }

    private fun updateRecyclerView(data: List<String>) {
        // 更新 RecyclerView 的数据
        recyclerViewAdapter.submitList(data)
    }
}
