package com.example.edmund.widget

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class CustomWebView(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {
    var onScrollToBottomListener: (() -> Unit)? = null

    init {
        this.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            // 当用户滚动到 WebView 的底部时触发
            if (scrollY + height >= computeVerticalScrollRange()) {
                onScrollToBottomListener?.invoke()  // 调用外部定义的监听器
            }
        }
    }
}
