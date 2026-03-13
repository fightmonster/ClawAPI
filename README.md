# Claw API

Android 应用，支持 Google 账号登录，访问 Google Drive 和 Gmail。

## 功能

### 🔐 Google 登录
- 使用 Google Sign-In API
- OAuth 2.0 认证

### 📁 Google Drive
- 列出文件和文件夹
- 搜索文件
- 查看文件详情（名称、大小、修改时间）
- 支持多种文件类型图标

### 📧 Gmail
- 列出收件箱邮件
- 搜索邮件
- 显示未读邮件标记
- 预览邮件内容

## 技术栈

- **Kotlin** - 主要开发语言
- **Jetpack Compose** - 现代 UI 框架
- **Material Design 3** - UI 设计
- **Google Play Services** - Google 登录
- **Google Drive API v3** - Drive 访问
- **Gmail API v1** - Gmail 访问
- **Coroutines** - 异步处理
- **Navigation Compose** - 页面导航

## 配置步骤

### 1. 创建 Google Cloud 项目

1. 访问 [Google Cloud Console](https://console.cloud.google.com/)
2. 创建新项目或选择现有项目
3. 启用以下 API：
   - Google Drive API
   - Gmail API

### 2. 配置 OAuth 2.0

1. 转到"API 和服务" > "凭据"
2. 创建 OAuth 2.0 客户端 ID
3. 选择"Android"应用类型
4. 输入应用包名：`com.claw.api`
5. 添加 SHA-1 证书指纹（通过 `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android` 获取）

### 3. 更新代码

在 `app/src/main/java/com/claw/api/data/AuthManager.kt` 中替换：

```kotlin
const val SERVER_CLIENT_ID = "YOUR_SERVER_CLIENT_ID_HERE"
```

为你的 OAuth 2.0 客户端 ID。

### 4. 编译运行

```bash
./gradlew assembleDebug
```

## 项目结构

```
ClawAPI/
├── app/
│   ├── src/main/
│   │   ├── java/com/claw/api/
│   │   │   ├── data/           # 数据层
│   │   │   │   ├── AuthManager.kt
│   │   │   │   ├── drive/
│   │   │   │   └── gmail/
│   │   │   ├── ui/             # UI 层
│   │   │   │   ├── drive/
│   │   │   │   ├── gmail/
│   │   │   │   ├── login/
│   │   │   │   └── theme/
│   │   │   └── MainActivity.kt
│   │   └── res/                # 资源文件
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## 权限

应用需要以下权限：
- `INTERNET` - 访问网络
- `ACCESS_NETWORK_STATE` - 检查网络状态

## 注意事项

⚠️ **重要**：
- 此应用使用 Google Drive 和 Gmail API，需要在 Google Cloud Console 配置 OAuth 2.0
- 首次运行需要用户授权访问 Drive 和 Gmail
- 建议使用 HTTPS 进行 API 调用（已在代码中实现）

## 许可证

MIT License
