package com.example.edmund.data.parse

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.InputStream
import com.shockwave.pdfium.PdfiumCore
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class PdfParser(private val context: Context, private val pdfiumCore: PdfiumCore) {

    /**
     * 提取PDF文件的元数据（例如标题、作者）
     */
    fun extractMetadata(uri: Uri): Map<String, String?> {
        val metadata = mutableMapOf<String, String?>()
        try {
            // 打开 PDF 文件
            val document = pdfiumCore.newDocument(getFileDescriptorFromUri(uri))
            val documentMeta = pdfiumCore.getDocumentMeta(document)

            // 获取 PDF 文档的元数据
            val title = documentMeta.title
            val author = documentMeta.author
            val subject = documentMeta.subject

            // 保存元数据
            metadata["Title"] = title
            metadata["Author"] = author
            metadata["Subject"] = subject

            // 关闭文档
            pdfiumCore.closeDocument(document)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return metadata
    }

    /**
     * 提取PDF文件的封面图
     */
    fun extractCoverImage(uri: Uri): Bitmap? {
        return try {
            // 打开 PDF 文件
            val document = pdfiumCore.newDocument(getFileDescriptorFromUri(uri))

            // 获取 PDF 的第一页
            val pageCount = pdfiumCore.getPageCount(document)
            if (pageCount > 0) {
                // 只提取第一页作为封面
                val page = pdfiumCore.openPage(document, 0)

                // 设置渲染的 Bitmap 大小
                val width = pdfiumCore.getPageWidth(document, 0)
                val height = pdfiumCore.getPageWidth(document, 0)

                // 创建位图
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

                // 渲染第一页到位图
                pdfiumCore.renderPageBitmap(document, bitmap, 0, 0, 0, width, height)

                // 关闭文档
                pdfiumCore.closeDocument(document)

                bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileDescriptorFromUri(uri: Uri): ParcelFileDescriptor {
        val resolver: ContentResolver = context.contentResolver
        return resolver.openFileDescriptor(uri, "r") ?: throw IOException("Unable to open file descriptor for Uri: $uri")
    }

}

