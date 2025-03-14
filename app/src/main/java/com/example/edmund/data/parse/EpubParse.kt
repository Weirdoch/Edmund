package com.example.edmund.data.parse

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Metadata
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.Spine
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.InputStream


object EpubParse {

    private const val TAG = "EpubParseUtils"

    // 解析 EPUB 文件，返回 Book 对象，捕获异常并封装到 Result 中
    fun parseEpub(inputStream: InputStream): Result<Book> {
        return try {
            val reader = EpubReader()
            val book = reader.readEpub(inputStream)
            Result.success(book)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing EPUB", e)
            Result.failure(e)  // 失败时返回异常
        }
    }

    // 获取 EPUB 的元数据信息
    fun getBookInfo(book: Book): String? {
        return try {
            val metadata: Metadata = book.metadata
            """
                作者：${metadata.authors.getOrNull(0) ?: "未知"}
                出版社：${metadata.publishers.getOrNull(0) ?: "未知"}
                出版时间：${metadata.dates.getOrNull(0)?.value ?: "未知"}
                书名：${metadata.titles.getOrNull(0) ?: "未知"}
                简介：${metadata.descriptions.getOrNull(0) ?: "暂无简介"}
                语言：${metadata.language ?: "未知"}
            """.trimIndent()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting book info", e)
            null
        }
    }

    // 获取封面图，如果没有封面图则返回 null
    fun getCoverImage(book: Book): Result<Bitmap> {
        return try {
            val resources = book.resources
            val coverResource: Resource? = resources.getById("cover")
            coverResource?.let {
                val bitmap = BitmapFactory.decodeStream(it.inputStream)
                Result.success(bitmap)
            } ?: Result.failure(IllegalArgumentException("No cover image found"))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cover image", e)
            Result.failure(e)
        }
    }

    // 获取章节内容的 HTML
    fun getChapterHtml(book: Book, chapterIndex: Int): Result<String> {
        return try {
            val spine: Spine = book.spine
            val spineReferences = spine.spineReferences
            if (spineReferences.isNotEmpty() && chapterIndex in 0 until spineReferences.size) {
                val resource: Resource = spineReferences[chapterIndex].resource
                val htmlContent = String(resource.data)
                Result.success(htmlContent)
            } else {
                Result.failure(IllegalArgumentException("Invalid chapter index"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chapter HTML", e)
            Result.failure(e)
        }
    }

    // 解析 HTML 数据，提取所有链接
    fun parseHtmlLinks(html: String): Result<List<EpubBean>> {
        return try {
            val doc = Jsoup.parse(html)
            val elements: Elements = doc.getElementsByTag("a") // 获取所有的 a 标签
            val epubBeans = elements.map { link ->
                val linkHref = link.attr("href") // 获取 a 标签的 href 属性
                val text = link.text()
                EpubBean(linkHref, text)
            }
            Result.success(epubBeans)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing HTML links", e)
            Result.failure(e)
        }
    }
}

data class EpubBean(
    val linkHref: String,
    val text: String
)

