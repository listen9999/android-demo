package com.partner.demo.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.partner.demo.bridge.WebViewBridge
import com.partner.demo.databinding.ActivityWebviewBinding
import com.partner.demo.model.TransactionResult

/**
 * WebView授权页面
 * 加载安全交易系统的授权URL
 */
class WebViewActivity : AppCompatActivity(), WebViewBridge.TransactionCallback {

    private lateinit var binding: ActivityWebviewBinding
    private var transactionId: String? = null

    companion object {
        private const val EXTRA_AUTH_URL = "auth_url"

        fun createIntent(context: Context, authUrl: String): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_AUTH_URL, authUrl)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val authUrl = intent.getStringExtra(EXTRA_AUTH_URL) ?: run {
            finish()
            return
        }

        setupWebView(authUrl)
        setupToolbar()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(authUrl: String) {
        binding.webView.apply {
            // WebView设置
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false

            // 添加JavaScript桥接
            addJavascriptInterface(
                WebViewBridge(this@WebViewActivity, this@WebViewActivity),
                "AndroidBridge"
            )

            // WebViewClient
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    // 所有链接都在WebView内打开
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                }
            }

            // WebChromeClient用于处理加载进度
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (newProgress < 100) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.progressBar.progress = newProgress
                    } else {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }

            // 加载授权URL
            loadUrl(authUrl)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // 用户点击返回，通知取消交易
            onTransactionResult(TransactionResult(false, null))
        }
    }

    // ========== WebViewBridge.TransactionCallback ==========

    override fun onTransactionResult(result: TransactionResult) {
        // 保存交易结果
        transactionId = result.transactionId

        // 返回结果给MainActivity
        val resultIntent = Intent().apply {
            putExtra("success", result.success)
            result.transactionId?.let { putExtra("transactionId", it) }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onClose() {
        // WebView请求关闭
        finish()
    }

    // 返回键处理
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            // 取消交易
            onTransactionResult(TransactionResult(false, null))
        }
    }

    override fun onDestroy() {
        binding.webView.apply {
            removeJavascriptInterface("AndroidBridge")
            destroy()
        }
        super.onDestroy()
    }
}