package com.example.edmund.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.edmund.R
import com.example.edmund.databinding.ActivityMainBinding
import com.example.edmund.ui.adapter.ScreenAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding  // 声明 ViewBinding 对象
    private lateinit var screenAdapter: ScreenAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 ViewBinding 绑定视图
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 创建适配器并设置给 ViewPager2
        screenAdapter = ScreenAdapter(this)
        binding.viewPager.adapter = screenAdapter

        // 监听 BottomNavigationView 的点击事件
        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            val position = when (item.itemId) {
                R.id.nav_library -> 0
                R.id.nav_history -> 1
                R.id.nav_browse -> 2
                else -> 0
            }

            // 根据点击的项切换 ViewPager2 的页面
            binding.viewPager.setCurrentItem(position, true)
            true
        }

        // 监听 ViewPager2 的页面变化
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 更新底部导航栏的选中项
                when (position) {
                    0 -> binding.bottomNav.selectedItemId = R.id.nav_library
                    1 -> binding.bottomNav.selectedItemId = R.id.nav_history
                    2 -> binding.bottomNav.selectedItemId = R.id.nav_browse
                }
            }
        })
    }
}
