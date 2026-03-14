# 📱 ClawAPI - Android Google API 集成

[![Android CI](https://github.com/fightmonster/ClawAPI/actions/workflows/android.yml/badge.svg)](https://github.com/fightmonster/ClawAPI/actions/workflows/android.yml)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

一个现代化的 Android 应用，支持用户使用自己的 Google 账号登录，访问 Google Drive 和 Gmail。

## ✨ 功能特性

### 🔐 Google 登录
- OAuth 2.0 安全认证
- 自动检测登录状态
- 一键退出登录

### 📁 Google Drive
- 📂 列出所有文件和文件夹
- 🔍 实时搜索文件
- 📊 显示文件详情（名称、大小、修改时间）
- 🎨 智能文件图标（图片、视频、PDF、文件夹等）
- 📥 文件下载（开发中）

### 📧 Gmail
- 📬 列出收件箱邮件
- 🔍 搜索邮件内容
- 🔵 未读邮件标记
- 👀 邮件预览（发件人、主题、摘要）
- 📖 邮件详情（开发中）

## 🎯 架构说明

```
开发者配置（一次性）           用户使用（每个用户）
┌─────────────────┐          ┌──────────────────┐
│ Google Cloud    │          │  下载 App        │
│ ├─ 创建项目     │          │  ├─ 点击登录     │
│ ├─ 启用 API     │   ───>   │  ├─ 授权账号     │
│ └─ 配置 OAuth   │          │  └─ 访问数据     │
└─────────────────┘          └──────────────────┘
```

**关键点**：
- ✅ 所有用户共用一个 OAuth Client ID
- ✅ 每个用户用自己的 Google 账号登录
- ✅ 每个用户只能访问自己的 Drive/Gmail

## 🚀 快速开始

### 前置要求

- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17+
- Android SDK 34
- Google Cloud 账号

### 配置步骤

#### 1. 克隆项目

```bash
git clone https://github.com/fightmonster/ClawAPI.git
cd ClawAPI
```

#### 2. 配置 Google Cloud 项目

详细步骤请查看：[Google Cloud 配置指南](docs/GOOGLE_CLOUD_SETUP.md)

**快速摘要**：
1. 创建 Google Cloud 项目
2. 启用 Drive API 和 Gmail API
3. 配置 OAuth 同意屏幕（外部用户类型）
4. 创建 OAuth 2.0 Client ID（Android 类型）
5. 添加 SHA-1 证书指纹

#### 3. 获取 SHA-1 证书指纹

详细方法请查看：[获取 SHA-1 指南](docs/GET_SHA1.md)

**快速方法**（Debug 签名）：
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### 4. 更新代码

编辑 `app/src/main/java/com/claw/api/data/AuthManager.kt`：

```kotlin
const val SERVER_CLIENT_ID = "你的客户端ID.apps.googleusercontent.com"
```

#### 5. 编译运行

```bash
# 编译 Debug APK
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 或在 Android Studio 中点击 Run
```

## 📖 详细文档

- [Google Cloud 配置指南](docs/GOOGLE_CLOUD_SETUP.md) - 完整的 OAuth 配置步骤
- [获取 SHA-1 指南](docs/GET_SHA1.md) - 多种获取 SHA-1 的方法
- [发布指南](docs/RELEASE.md) - 如何发布到 Google Play（即将推出）

## 🛠️ 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Kotlin** | 1.9.22 | 主要开发语言 |
| **Jetpack Compose** | BOM 2024.02.00 | 现代 UI 框架 |
| **Material Design 3** | Latest | UI 设计系统 |
| **Google Play Services Auth** | 21.0.0 | Google 登录 |
| **Google Drive API** | v3-rev20240123 | Drive 访问 |
| **Gmail API** | v1-rev20231218 | Gmail 访问 |
| **Coroutines** | 1.7.3 | 异步处理 |
| **Navigation Compose** | 2.7.6 | 页面导航 |
| **Lifecycle ViewModel** | 2.7.0 | 架构组件 |

## 📁 项目结构

```
ClawAPI/
├── app/
│   ├── src/main/
│   │   ├── java/com/claw/api/
│   │   │   ├── data/              # 数据层
│   │   │   │   ├── AuthManager.kt        # 认证管理
│   │   │   │   ├── drive/                # Drive 数据仓库
│   │   │   │   │   └── DriveRepository.kt
│   │   │   │   └── gmail/                # Gmail 数据仓库
│   │   │   │       └── GmailRepository.kt
│   │   │   ├── ui/                # UI 层
│   │   │   │   ├── drive/                # Drive 界面
│   │   │   │   │   └── DriveViewModel.kt
│   │   │   │   ├── gmail/                # Gmail 界面
│   │   │   │   │   └── GmailViewModel.kt
│   │   │   │   ├── login/                # 登录界面
│   │   │   │   │   └── LoginViewModel.kt
│   │   │   │   └── theme/                # 主题配置
│   │   │   │       └── Theme.kt
│   │   │   └── MainActivity.kt    # 主活动
│   │   └── res/                   # 资源文件
│   └── build.gradle.kts           # 应用级构建配置
├── docs/                          # 文档
├── .github/workflows/             # GitHub Actions CI
├── build.gradle.kts              # 项目级构建配置
└── README.md                      # 本文件
```

## 🎨 设计模式

- **MVVM 架构**：清晰的数据流和生命周期管理
- **Repository 模式**：数据访问抽象层
- **单向数据流**：Compose + StateFlow
- **依赖注入**：通过 ViewModel 传递依赖

## 📊 GitHub Actions CI/CD

项目配置了自动构建流程：

- ✅ 每次 Push 自动编译
- ✅ Pull Request 检查
- ✅ 自动生成 Debug APK
- ✅ 自动生成 Release APK（未签名）

下载构建产物：[Actions](https://github.com/fightmonster/ClawAPI/actions)

## 🔐 安全性

### OAuth 2.0 流程

```
用户点击登录
    ↓
Google Sign-In SDK
    ↓
用户授权（弹出 Google 登录界面）
    ↓
获取 OAuth Token
    ↓
访问 Drive/Gmail API
```

### 权限说明

应用请求以下权限：
- `https://www.googleapis.com/auth/drive.readonly` - 只读访问 Drive
- `https://www.googleapis.com/auth/gmail.readonly` - 只读访问 Gmail

### 数据隐私

- ✅ 用户数据只存储在用户设备上
- ✅ 不会上传到任何第三方服务器
- ✅ OAuth Token 安全存储在系统 Keystore
- ✅ 用户可随时撤销授权

## 🐛 故障排除

### 常见问题

**Q: 登录时提示"无法连接到 Google 服务"**
- 检查 SHA-1 是否正确配置到 Google Cloud Console
- 确认包名是 `com.claw.api`
- 检查网络连接

**Q: 登录成功但无法访问 Drive/Gmail**
- 确认已在 Google Cloud Console 启用相应 API
- 检查 OAuth 同意屏幕的作用域配置

**Q: Debug 版本能用，Release 版本不能登录**
- Debug 和 Release 的 SHA-1 不同
- 需要为 Release 签名单独配置 OAuth Client ID

更多问题请查看：[故障排除指南](docs/TROUBLESHOOTING.md)（即将推出）

## 🗺️ 路线图

- [ ] 文件下载功能
- [ ] 邮件详情页面
- [ ] 文件预览（图片、PDF）
- [ ] 离线缓存
- [ ] 多账号支持
- [ ] 深色模式
- [ ] 平板适配
- [ ] Wear OS 支持

## 🤝 贡献

欢迎贡献！请查看 [贡献指南](CONTRIBUTING.md)（即将推出）。

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件。

## 🙏 致谢

- [Google APIs for Android](https://developers.google.com/android)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)

## 📞 联系方式

- **Issues**: [GitHub Issues](https://github.com/fightmonster/ClawAPI/issues)
- **Discussions**: [GitHub Discussions](https://github.com/fightmonster/ClawAPI/discussions)

---

**如果这个项目对你有帮助，请给一个 ⭐️ Star！**

