package com.example.edmund.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.edmund.R
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore

//class PdfAdapter(
//    private val context: Context,
//    private val pdfDocument: PdfDocument,
//    private val totalPages: Int
//) : RecyclerView.Adapter<PdfAdapter.PdfViewHolder>() {
//
//    private val pdfiumCore = PdfiumCore(context)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
//        val view = LayoutInflater.from(context).inflate(R.layout.pdf_page_layout, parent, false)
//        return PdfViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
//
//
//        pdfiumCore.openPage(pdfDocument, position)
//
//        val width = pdfiumCore.getPageWidth(pdfDocument, position)
//        val height = pdfiumCore.getPageHeight(pdfDocument, position)
//
//        // Create a Bitmap from the page
//        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, position, 0, 0, width, height)
//
//
////        val page = pdfDocument[position]
//
//        // 渲染当前页面为 Bitmap
////        val bitmap: Bitmap = pdfiumCore.renderPageBitmap(page, 0, 0, page.width, page.height)
//
//        holder.imageView.setImageBitmap(bitmap)
//
//
//    }
//
//    override fun getItemCount(): Int {
//        return totalPages
//    }
//
//    inner class PdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imageView: ImageView = itemView.findViewById(R.id.imageView)
//    }
//}
