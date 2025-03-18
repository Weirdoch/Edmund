package com.example.edmund

import android.app.Application
import com.shockwave.pdfium.PdfiumCore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : Application() {

    // 你可以将 PdfiumCore 作为全局变量持有
    companion object {
        var pdfiumCore: PdfiumCore? = null
    }

    override fun onCreate() {
        super.onCreate()

        // 在应用启动时初始化 PdfiumCore
        initializePdfium()
    }

    private fun initializePdfium() {
        // 这里需要使用你的 PdfiumCore 初始化逻辑
        pdfiumCore = PdfiumCore(this)
    }


}
