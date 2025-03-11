package com.example.edmund.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.edmund.ui.fragment.BrowseFragment
import com.example.edmund.ui.fragment.HistoryFragment
import com.example.edmund.ui.fragment.LibraryFragment

class ScreenAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LibraryFragment()
            1 -> HistoryFragment()
            2 -> BrowseFragment()
            else -> HistoryFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3 // 共三个页面
    }
}
