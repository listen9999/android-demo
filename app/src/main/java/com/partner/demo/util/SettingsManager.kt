package com.partner.demo.util

import android.content.Context

object SettingsManager {

    private const val PREFS_NAME = "partner_settings"
    private const val KEY_BACKEND_URL = "backend_url"

    // 默认URL
    const val DEFAULT_URL = "http://10.0.2.2:8081/"

    fun getBackendUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BACKEND_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    fun setBackendUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BACKEND_URL, url).apply()
    }
}