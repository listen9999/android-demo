package com.partner.demo.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.partner.demo.NetworkModule
import com.partner.demo.databinding.ActivityMainBinding
import com.partner.demo.model.TransferRequest
import com.partner.demo.model.TransferResponse
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * 主页面 - 转账表单
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val apiService by lazy { NetworkModule.create<com.partner.demo.network.PartnerApiService>() }

    private var pendingTransactionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        // 设置默认值
        binding.etCardNumber.setText("1234567890123456")
        binding.etAmount.setText("100.00")

        // 转账按钮
        binding.btnTransfer.setOnClickListener {
            initiateTransfer()
        }
    }

    private fun initiateTransfer() {
        val cardNumber = binding.etCardNumber.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val currency = "USD"

        // 验证输入
        if (cardNumber.length < 16) {
            showError("请输入正确的卡号")
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            showError("请输入有效金额")
            return
        }

        // 显示加载状态
        showLoading(true)

        // 调用API
        lifecycleScope.launch {
            try {
                val request = TransferRequest(cardNumber, amount, currency)
                val response = apiService.initiateTransfer(request)

                handleTransferResponse(response)

            } catch (e: Exception) {
                showLoading(false)
                showError("网络错误: ${e.message}")
            }
        }
    }

    private fun handleTransferResponse(response: Response<TransferResponse>) {
        showLoading(false)

        if (!response.isSuccessful) {
            showError("请求失败: ${response.code()}")
            return
        }

        val body = response.body()
        if (body == null) {
            showError("响应为空")
            return
        }

        if (body.success && body.authUrl != null) {
            // 保存交易ID
            pendingTransactionId = body.transactionId

            // 打开WebView进行授权
            val intent = WebViewActivity.createIntent(this, body.authUrl)
            startActivityForResult(intent, REQUEST_CODE_AUTH)

        } else {
            showError(body.message ?: "转账失败")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_AUTH) {
            if (resultCode == RESULT_OK && data != null) {
                val success = data.getBooleanExtra("success", false)
                val transactionId = data.getStringExtra("transactionId")

                if (success) {
                    showSuccess(transactionId)
                } else {
                    showCancelled()
                }
            } else {
                showCancelled()
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnTransfer.isEnabled = !loading
        binding.btnTransfer.text = if (loading) "处理中..." else "确认转账"
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("错误")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }

    private fun showSuccess(transactionId: String?) {
        AlertDialog.Builder(this)
            .setTitle("交易成功")
            .setMessage("交易已完成\n交易ID: ${transactionId ?: pendingTransactionId}")
            .setPositiveButton("确定", null)
            .show()
    }

    private fun showCancelled() {
        Toast.makeText(this, "交易已取消", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_AUTH = 1001
    }
}