package com.example.edmund.ui.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.edmund.R
import com.example.edmund.data.parse.PdfCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PdfPageAdapter(
    private val pdfCacheManager: PdfCacheManager,
    private val totalPageCount: Int
) : RecyclerView.Adapter<PdfPageAdapter.PdfPageViewHolder>() {

    private val MAX_CACHE_SIZE = 8

    // 使用 LinkedHashMap 作为 LRU 缓存，存储已加载的页面数据
    private val loadedPages = object : LinkedHashMap<Int, Pair<Bitmap, Bitmap>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Pair<Bitmap, Bitmap>>?): Boolean {
            // 当缓存的大小超过最大缓存限制时，移除最旧的页面
            return size > MAX_CACHE_SIZE
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pdf_page_layout, parent, false)
        return PdfPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfPageViewHolder, position: Int) {
        val cachedData = loadedPages[position]
        if (cachedData == null) {
            // 显示正在加载的 ProgressBar
            holder.progressBar.visibility = View.VISIBLE
            holder.imageViewLeft.visibility = View.GONE
            holder.imageViewRight.visibility = View.GONE

            GlobalScope.launch(Dispatchers.Main) {
                // 使用 IO 线程加载位图
                val leftBitmap = withContext(Dispatchers.IO) { pdfCacheManager.getLeftPageBitmap(position) }
                val rightBitmap = withContext(Dispatchers.IO) { pdfCacheManager.getRightPageBitmap(position) }

                // 加载完成后更新 UI
                holder.progressBar.visibility = View.GONE
                holder.imageViewLeft.visibility = View.VISIBLE
                holder.imageViewRight.visibility = View.VISIBLE

                holder.imageViewLeft.setImageBitmap(leftBitmap)
                holder.imageViewRight.setImageBitmap(rightBitmap)

                // 缓存加载的位图
                loadedPages[position] = Pair(leftBitmap, rightBitmap)
            }
        } else {
            // 使用缓存的数据
            holder.imageViewLeft.setImageBitmap(cachedData.first)
            holder.imageViewRight.setImageBitmap(cachedData.second)
        }
    }

    override fun getItemCount(): Int {
        return (totalPageCount + 1) / 2 // 获取 PDF 总页数
    }

    class PdfPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progressBar: View = itemView.findViewById(R.id.progressBar)
        val imageViewLeft: ImageView = itemView.findViewById(R.id.imageViewLeft)
        val imageViewRight: ImageView = itemView.findViewById(R.id.imageViewRight)
    }
}
