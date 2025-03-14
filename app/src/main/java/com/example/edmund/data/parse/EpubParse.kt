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

object EpubParse{

    private const val TAG = "EpubParseUtils"

    // 解析 EPUB 文件，返回 Book 对象
    fun parseEpub(inputStream: InputStream): Book? {
        return try {
            val reader = EpubReader()
            reader.readEpub(inputStream)  // 读取 EPUB 内容
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 获取 EPUB 的元数据信息
    fun getBookInfo(book: Book): String {
        val metadata: Metadata = book.metadata
        return """
            作者：${metadata.authors[0]}
            出版社：${metadata.publishers[0]}
            出版时间：${metadata.dates[0].value}
            书名：${metadata.titles[0]}
            简介：${metadata.descriptions[0]}
            语言：${metadata.language}
        """.trimIndent()
    }

    // 获取封面图，如果没有封面图则返回 null
    fun getCoverImage(book: Book): Bitmap? {
        return try {
            val resources = book.resources
            val coverResource: Resource? = resources.getById("cover")
            coverResource?.let {
                BitmapFactory.decodeStream(it.inputStream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cover image", e)
            null
        }
    }

    // 获取章节内容的 HTML
    fun getChapterHtml(book: Book): String? {
        return try {
            val spine: Spine = book.spine
            val spineReferences = spine.spineReferences
            if (spineReferences.isNotEmpty()) {
                val resource: Resource = spineReferences[14].resource // 获取带章节信息的 HTML 页面
                String(resource.data)
            } else {
                Log.w(TAG, "No chapters found in EPUB")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chapter HTML", e)
            null
        }
    }

    // 解析 HTML 数据，提取所有链接
    fun parseHtmlLinks(html: String): List<EpubBean> {
        val epubBeans = mutableListOf<EpubBean>()
        try {
            val doc = Jsoup.parse(html)
            val elements: Elements = doc.getElementsByTag("a") // 获取所有的 a 标签
            for (link in elements) {
                val linkHref = link.attr("href") // 获取 a 标签的 href 属性
                val text = link.text()
                val epubBean = EpubBean(linkHref, text)
                epubBeans.add(epubBean)
                Log.i(TAG, "parseHtmlData: linkHref=$linkHref text=$text")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing HTML links", e)
        }
        return epubBeans
    }
}


data class EpubBean(
    val linkHref: String,
    val text: String
)
