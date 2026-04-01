package com.partner.demo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.partner.demo.NetworkModule
import com.partner.demo.databinding.ActivitySettingsBinding
import com.partner.demo.util.SettingsManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        // 显示当前配置的URL
        binding.etBackendUrl.setText(NetworkModule.getCurrentUrl())

        // 保存按钮
        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun saveSettings() {
        val url = binding.etBackendUrl.text.toString().trim()

        // 验证URL格式
        if (url.isEmpty()) {
            showError("请输入服务地址")
            return
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            showError("URL必须以 http:// 或 https:// 开头")
            return
        }

        if (!url.endsWith("/")) {
            showError("URL必须以 / 结尾")
            return
        }

        // 保存并更新
        NetworkModule.updateUrl(this, url)

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()

        // 返回主页面
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}