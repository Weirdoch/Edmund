package com.example.edmund.data.parse

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.openjdk.tools.javac.util.StringUtils
import java.io.IOException
import java.io.InputStream

//
//class EpubParse {
//
//
//    private var book: Book? = null
//    private var mAdatper: MyAdatper? = null
//    private val indexTitleList: MutableList<EpubBean> = ArrayList<EpubBean>()
//
//
//    private fun initView() {
//        mRecycler!!.layoutManager = LinearLayoutManager(this)
//        mAdatper = MyAdatper(indexTitleList, this)
//        mRecycler!!.adapter = mAdatper
//
//        try {
//            val reader = EpubReader()
//            val `in`: InputStream = getAssets().open("176116.epub")
//            //            InputStream in = getAssets().open("qhf.epub");
////            InputStream in = getAssets().open("algorithms.epub");
//            book = reader.readEpub(`in`)
//
//            //获取封面图方法一：
//            /* Bitmap coverImage = BitmapFactory.decodeStream(book.getCoverImage().getInputStream());
//            if (coverImage!=null) {
//                mImageView.setImageBitmap(coverImage);
//            }else {
//                Log.i(TAG, "onCreate: mImageView is null");
//            }*/
//            //  获取封面图方法二：
//            /*nl.siegmann.epublib.domain.Resources resources = book.getResources();
//            Resource res = resources.getById("cover");
//            byte[] data = res.getData();
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            mImage.setImageBitmap(bitmap);*/
//            val metadata: Metadata = book.getMetadata()
//
//            val buffer = StringBuffer()
//            for (s in metadata.getDescriptions()) {
//                buffer.append("$s ")
//            }
//
//            /*String bookInfo = "作者：" + metadata.getAuthors() +
//                    "\n出版社：" + metadata.getPublishers() +
//                    "\n出版时间：" + metadata.getDates() +
//                    "\n书名：" + metadata.getTitles() +
//                    "\n简介：" + metadata.getDescriptions() +
//                    "\n语言：" + metadata.getLanguage() +
//                    "\n\n封面图：";*/
//            val bookInfo = ((((("""
//    ${("作者：" + metadata.getAuthors().get(0))}
//    出版社：${metadata.getPublishers().get(0)}
//    """.trimIndent()).toString() +
//                    "\n出版时间：" + TimeUtils.getStringData(
//                metadata.getDates().get(0).getValue()
//            )).toString() +
//                    "\n书名：" + metadata.getTitles().get(0)).toString() +
//                    "\n简介：" + metadata.getDescriptions().get(0)).toString() +
//                    "\n语言：" + metadata.getLanguage()).toString() +
//                    "\n\n封面图："
//
//            mTvText!!.text = bookInfo
//
//            Log.i(TAG, "onCreate: bookInfo=$bookInfo")
//
//            // 书籍的阅读顺序，是一个线性的顺序。通过Spine可以知道应该按照怎样的章节,顺序去阅读，
//            // 并且通过Spine可以找到对应章节的内容。
//            val spine = book.getSpine()
//
//            val spineReferences = spine.spineReferences
//            if (spineReferences != null && spineReferences.size > 0) {
//                val resource = spineReferences[1].resource //获取带章节信息的那个html页面
//
//                Log.i(
//                    TAG,
//                    "initView: book=" + resource.id + "  " + resource.title + "  " + resource.size + " "
//                )
//
//                val data = resource.data //和 resource.getInputStream() 返回的都是html格式的文章内容，只不过读取方式不一样
//                val strHtml: String = StringUtils.bytes2Hex(data)
//                Log.i(TAG, "initView: strHtml= $strHtml")
//
//                parseHtmlData(strHtml)
//
//                /*  InputStream inputStream = resource.getInputStream();
//                String strHtml = StringUtils.convertStreamToString(inputStream);
//                Log.i(TAG, "initView: strHtml=" + strHtml);*/
//            } else {
//                Log.i(TAG, "initView: spineReferences is null")
//            }
//
//            // 获取所有章节内容。测试发现和 spine.getSpineReferences() 效果差不多
//            /* List<Resource> contents = book.getContents();
//            if (contents != null && contents.size() > 0) {
//                try {
//                    Resource resource = contents.get(1);
//                    //byte[] data = resource.getData();
//                    InputStream inputStream = resource.getInputStream();
//                    String dddd = StringUtils.convertStreamToString(inputStream);
//                    Log.i(TAG, "onCreate: dddd=" + dddd);
//                    //mTextView.setText(Html.fromHtml(dddd));
//                    //  mWebView.loadDataWithBaseURL(null, dddd, "text/html", "utf-8", null);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Log.i(TAG, "onCreate: contents is null");
//            }*/
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    /**
//     * 解析html
//     */
//    @Throws(IOException::class)
//    private fun parseHtmlData(strHtml: String) {
//        val doc: Document = Jsoup.parse(strHtml)
//        Log.i(TAG, "parseHtmlData:  doc.title();=" + doc.title())
//        val eles: Elements = doc.getElementsByTag("a") // a标签
//        // 遍历Elements的每个Element
//        var epubBean: EpubBean
//        for (link in eles) {
//            val linkHref: String = link.attr("href") // a标签的href属性
//            val text: String = link.text()
//            epubBean = EpubBean()
//            epubBean.href = linkHref
//            epubBean.tilte = text
//            indexTitleList.add(epubBean)
//            Log.i(TAG, "parseHtmlData: linkHref=$linkHref text=$text")
//        }
//    }
//
//    private class MyAdatper(mStrings: List<EpubBean>, context: Context?) :
//        RecyclerView.Adapter<MyAdatper.ViewHolder>() {
//        private val mInflater: LayoutInflater = LayoutInflater.from(context)
//
//        private val mStrings: List<EpubBean> = mStrings
//
//        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            var mTextView: TextView = itemView.findViewById<View>(R.id.book_title) as TextView
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            val view: View = mInflater.inflate(R.layout.layout_item, null)
//            val holder: ViewHolder = ViewHolder(view)
//            return holder
//        }
//
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            holder.mTextView.setText(mStrings[position].tilte)
//            holder.mTextView.setOnClickListener { //通过href获取
//                val href: String = mStrings[position].href
//                val intent: Intent =
//                    Intent(
//                        this@MainActivity,
//                        ChapterDetailActivity::class.java
//                    )
//                intent.putExtra("href", href)
//                startActivity(intent)
//            }
//        }
//
//        override fun getItemCount(): Int {
//            return mStrings.size
//        }
//    }
//}