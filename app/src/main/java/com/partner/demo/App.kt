package com.partner.demo

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化网络模块
        NetworkModule.init(this)
    }
}