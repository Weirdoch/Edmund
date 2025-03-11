package com.example.edmund.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edmund.databinding.ItemFileBinding
import java.io.File

class BrowseAdapter(private val onFileSelected: (DocumentFile) -> Unit) : ListAdapter<DocumentFile, BrowseAdapter.FileViewHolder>(FileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = getItem(position)
        holder.bind(file)
    }

    inner class FileViewHolder(private val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: DocumentFile) {
            // 显示文件名
            binding.fileName.text = file.name

            // 根据文件类型设置检查框的状态
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onFileSelected(file)  // 文件被选中
                } else {
                    onFileSelected(file)  // 文件被取消选择
                }
            }
        }
    }

    // 用 DiffUtil 优化列表更新
    class FileDiffCallback : DiffUtil.ItemCallback<DocumentFile>() {
        override fun areItemsTheSame(oldItem: DocumentFile, newItem: DocumentFile): Boolean {
            return oldItem.uri == newItem.uri  // 比较 URI 判断是否为同一文件
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DocumentFile, newItem: DocumentFile): Boolean {
            return oldItem == newItem
        }
    }
}
