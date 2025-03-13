package com.example.edmund.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edmund.data.dto.BookEntity
import com.example.edmund.databinding.ItemLibraryBinding

class LibraryAdapter : ListAdapter<BookEntity, LibraryAdapter.LibraryViewHolder>(BookEntityDiffCallback()) {

    private var onItemClickListener: ((BookEntity) -> Unit)? = null
    private var onItemLongClickListener: ((BookEntity, Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (BookEntity) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (BookEntity, Int) -> Unit) {
        onItemLongClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val binding = ItemLibraryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LibraryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book, position)
    }

    inner class LibraryViewHolder(private val binding: ItemLibraryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BookEntity, position: Int) {
            binding.bookTitle.text = book.title
            binding.bookAuthor.text = book.author ?: "未知作者"

            binding.root.setOnClickListener {
                onItemClickListener?.invoke(book)
            }

            binding.root.setOnLongClickListener {
                onItemLongClickListener?.invoke(book, adapterPosition)
                true
            }
        }
    }

    class BookEntityDiffCallback : DiffUtil.ItemCallback<BookEntity>() {
        override fun areItemsTheSame(oldItem: BookEntity, newItem: BookEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookEntity, newItem: BookEntity): Boolean {
            return oldItem == newItem
        }
    }

}
