package com.partner.demo.bridge

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.partner.demo.model.TransactionResult
import org.json.JSONObject

/**
 * WebView JavaScript Bridge
 * 接收来自授权页面的回调
 */
class WebViewBridge(
    private val context: Context,
    private val callback: TransactionCallback
) {

    interface TransactionCallback {
        /**
         * 交易结果回调
         * @param result 交易结果
         */
        fun onTransactionResult(result: TransactionResult)

        /**
         * 关闭WebView
         */
        fun onClose()
    }

    /**
     * 接收交易结果（从WebView调用）
     * JavaScript: window.AndroidBridge.onTransactionResult(JSON.stringify(result))
     */
    @JavascriptInterface
    fun onTransactionResult(resultJson: String) {
        try {
            val json = JSONObject(resultJson)
            val success = json.optBoolean("success", false)
            val transactionId = json.optString("transactionId", null)

            val result = TransactionResult(success, transactionId)

            // 在主线程回调
            Handler(Looper.getMainLooper()).post {
                callback.onTransactionResult(result)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                callback.onTransactionResult(TransactionResult(false, null))
            }
        }
    }

    /**
     * 关闭WebView（从WebView调用）
     * JavaScript: window.AndroidBridge.close()
     */
    @JavascriptInterface
    fun close() {
        Handler(Looper.getMainLooper()).post {
            callback.onClose()
        }
    }
}