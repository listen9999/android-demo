package com.partner.demo

import android.content.Context
import com.partner.demo.util.SettingsManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private var baseUrl: String = SettingsManager.DEFAULT_URL
    private var _retrofit: Retrofit? = null
    val retrofit: Retrofit get() = _retrofit!!

    fun init(context: Context) {
        // 从设置中读取URL
        baseUrl = SettingsManager.getBackendUrl(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        _retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 更新URL并重新初始化
    fun updateUrl(context: Context, newUrl: String) {
        baseUrl = newUrl
        SettingsManager.setBackendUrl(context, newUrl)
        init(context)
    }

    fun getCurrentUrl(): String = baseUrl

    inline fun <reified T> create(): T {
        return retrofit.create(T::class.java)
    }
}