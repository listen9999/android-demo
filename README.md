# 合作伙伴 Android Demo App

安全交易系统的Android客户端演示应用，展示如何在Android App中集成WebView授权流程。

## 快速开始（无需本地Android环境）

### 方案1：GitHub Actions自动构建（推荐）

1. 将项目推送到GitHub仓库
2. 进入仓库的 **Actions** 标签页
3. 选择 **Build Android App** 工作流
4. 点击 **Run workflow** 手动触发构建
5. 构建完成后，在 **Artifacts** 中下载 `app-debug.apk`
6. 安装到手机或模拟器

### 方案2：使用在线IDE

| 平台 | 说明 |
|------|------|
| [GitHub Codespaces](https://github.com/features/codespaces) | 在线VS Code环境 |
| [Gitpod](https://gitpod.io) | 免费在线开发环境 |
| [Android Studio在线版](https://developer.android.com/studio) | Firebase项目可用 |

### 方案3：Docker构建

```bash
# 使用Docker构建APK
docker run --rm -v $(pwd):/project mingc/android-build-box bash -c "
  cd /project &&
  ./gradlew assembleDebug
"

# APK位置: app/build/outputs/apk/debug/app-debug.apk
```

---

## 本地开发环境

### 前置要求

- JDK 17+
- Android SDK (可通过Android Studio安装)
- Android SDK 34

### 构建步骤

```bash
# 1. 初始化Gradle Wrapper（首次）
gradle wrapper

# 2. 构建Debug APK
./gradlew assembleDebug

# 3. 安装到设备
./gradlew installDebug

# APK位置
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 项目结构

```
app/src/main/java/com/partner/demo/
├── App.kt                    # Application类
├── NetworkModule.kt          # 网络模块配置
├── model/
│   └── Models.kt             # 数据模型
├── network/
│   └── PartnerApiService.kt  # API服务接口
├── bridge/
│   └── WebViewBridge.kt      # JS桥接类
└── ui/
    ├── MainActivity.kt       # 主页面（转账表单）
    └── WebViewActivity.kt    # WebView授权页面
```

---

## 配置说明

### 网络地址配置

修改 `NetworkModule.kt` 中的 `BASE_URL`：

| 环境 | 地址 |
|------|------|
| Android模拟器 | `http://10.0.2.2:8081/` |
| 真机调试 | `http://192.168.x.x:8081/` |

### 真机调试步骤

1. 确保手机和电脑在同一局域网
2. 查看电脑IP: `ipconfig` (Windows) 或 `ifconfig` (Mac/Linux)
3. 修改 `NetworkModule.kt` 中的 `BASE_URL`
4. 确保后端服务运行在 `0.0.0.0` 而非 `127.0.0.1`

---

## JavaScript Bridge

WebView通过`AndroidBridge`接口与App通信：

```javascript
// 通知交易结果
window.AndroidBridge.onTransactionResult(JSON.stringify({
    success: true,
    transactionId: "TXN_123456"
}));

// 关闭WebView
window.AndroidBridge.close();
```

### Android端实现

```kotlin
class WebViewBridge(
    private val context: Context,
    private val callback: TransactionCallback
) {
    @JavascriptInterface
    fun onTransactionResult(resultJson: String) {
        val json = JSONObject(resultJson)
        val success = json.optBoolean("success", false)
        val transactionId = json.optString("transactionId", null)
        callback.onTransactionResult(TransactionResult(success, transactionId))
    }

    @JavascriptInterface
    fun close() {
        callback.onClose()
    }
}
```

---

## 测试流程

1. 启动App
2. 输入卡号（默认: 1234567890123456）
3. 输入金额（默认: 100.00）
4. 点击"确认转账"
5. WebView打开授权页面
6. 输入验证码: **123456**
7. 点击"确认授权"
8. 自动关闭WebView，显示成功提示

---

## 测试数据

```yaml
API Key: test_partner_key
Secret Key: test_secret_key_12345
测试OTP: 123456
测试卡号: 1234567890123456
```

---

## 生产环境注意事项

1. **使用HTTPS** - 所有网络请求使用加密连接
2. **URL验证** - 验证授权URL的域名白名单
3. **证书校验** - 添加SSL证书固定
4. **代码混淆** - 启用ProGuard/R8
5. **签名验证** - 使用正式签名密钥

---

## 常见问题

### Q: 无法连接后端服务？

**A**: 检查以下配置：
1. 后端服务是否运行
2. 手机和电脑是否在同一网络
3. `BASE_URL` 是否使用正确的IP地址
4. 后端是否绑定 `0.0.0.0` 而非 `127.0.0.1`

### Q: WebView加载失败？

**A**: 检查以下配置：
1. AndroidManifest.xml 是否添加 `INTERNET` 权限
2. `android:usesCleartextTraffic="true"` 是否添加（允许HTTP）
3. 后端前端服务是否正常运行

### Q: JS Bridge不工作？

**A**: 确认：
1. WebView已启用JavaScript: `settings.javaScriptEnabled = true`
2. 已添加JS接口: `addJavascriptInterface(bridge, "AndroidBridge")`
3. 方法已添加 `@JavascriptInterface` 注解