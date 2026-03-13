# Google Cloud 配置指南

本指南将帮助你在 Google Cloud Console 中配置 OAuth 2.0，让所有用户都能使用自己的 Google 账号登录。

## 📋 配置流程概览

```
1. 创建 Google Cloud 项目
2. 启用 API
3. 配置 OAuth 同意屏幕
4. 创建 OAuth 2.0 Client ID
5. 配置签名证书
6. 测试应用
```

## 第一步：创建 Google Cloud 项目

### 1.1 访问 Google Cloud Console

打开：https://console.cloud.google.com/

### 1.2 创建新项目

1. 点击顶部导航栏的项目选择器
2. 点击"新建项目"
3. 输入项目名称：`ClawAPI`（或你喜欢的名称）
4. 选择组织（如果有）
5. 点击"创建"

### 1.3 选择项目

创建完成后，在项目选择器中选择你的项目。

## 第二步：启用 API

### 2.1 打开 API 库

左侧菜单 > **API 和服务** > **库**

### 2.2 启用 Google Drive API

1. 搜索"Google Drive API"
2. 点击搜索结果
3. 点击"启用"

### 2.3 启用 Gmail API

1. 搜索"Gmail API"
2. 点击搜索结果
3. 点击"启用"

## 第三步：配置 OAuth 同意屏幕

### 3.1 打开配置页面

左侧菜单 > **API 和服务** > **OAuth 同意屏幕**

### 3.2 选择用户类型

选择 **"外部"**（允许任何 Google 账号使用）

点击"创建"

### 3.3 填写应用信息

**OAuth 同意屏幕**：

- **应用名称**：`Claw API`
- **用户支持电子邮件地址**：你的邮箱
- **应用徽标**：（可选）上传应用图标
- **应用网域**：（可选）
  - 首页：`https://github.com/fightmonster/ClawAPI`
  - 隐私权政策：（可选）
  - 服务条款：（可选）
- **已授权网域**：（可选）
- **开发者联系信息**：你的邮箱

点击"保存并继续"

### 3.4 配置作用域

点击"添加或移除作用域"

搜索并添加以下作用域：

| 作用域 | 描述 |
|--------|------|
| `.../auth/drive.readonly` | 查看您的 Google Drive 文件 |
| `.../auth/gmail.readonly` | 查看您的 Gmail 邮件 |

或者手动输入：
```
https://www.googleapis.com/auth/drive.readonly
https://www.googleapis.com/auth/gmail.readonly
```

点击"更新" > "保存并继续"

### 3.5 添加测试用户（重要！）

在应用发布前，只能添加测试用户。

1. 点击"添加用户"
2. 输入你的 Gmail 地址
3. 点击"添加"
4. 可以添加最多 100 个测试用户

点击"保存并继续"

### 3.6 检查配置

查看配置摘要，确认无误后点击"返回信息中心"

## 第四步：创建 OAuth 2.0 Client ID

### 4.1 打开凭据页面

左侧菜单 > **API 和服务** > **凭据**

### 4.2 创建凭据

点击"创建凭据" > "OAuth 2.0 客户端 ID"

### 4.3 配置客户端 ID

**应用类型**：选择 **Android**

**名称**：`Claw API Android Client`（或任意名称）

**包名**：`com.claw.api`（必须与代码中一致！）

**SHA-1 证书指纹**：

需要从你的签名文件获取 SHA-1，详见：[获取 SHA-1 指南](GET_SHA1.md)

快速方法（Debug 签名）：
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

复制输出中的 SHA-1 值（格式：`XX:XX:XX:...`）

点击"创建"

### 4.4 记录客户端 ID

创建成功后，会显示：
- **客户端 ID**：类似 `123456789-abcdefg.apps.googleusercontent.com`
- **客户端密钥**：（Android 应用不需要）

**重要**：复制客户端 ID，下一步需要用到。

## 第五步：更新代码

### 5.1 编辑 AuthManager.kt

打开 `app/src/main/java/com/claw/api/data/AuthManager.kt`

找到第 15 行：
```kotlin
const val SERVER_CLIENT_ID = "YOUR_SERVER_CLIENT_ID_HERE"
```

替换为你的客户端 ID：
```kotlin
const val SERVER_CLIENT_ID = "123456789-abcdefg.apps.googleusercontent.com"
```

保存文件。

### 5.2 （可选）配置 Release 签名

如果你要发布 Release 版本，需要：

1. 创建签名密钥：
```bash
keytool -genkey -v -keystore claw-api.keystore -alias claw-api -keyalg RSA -keysize 2048 -validity 10000
```

2. 获取 Release SHA-1：
```bash
keytool -list -v -keystore claw-api.keystore -alias claw-api
```

3. 在 Google Cloud Console 添加另一个 OAuth Client ID（使用 Release SHA-1）

## 第六步：测试应用

### 6.1 编译运行

```bash
./gradlew installDebug
```

或在 Android Studio 中点击 Run。

### 6.2 测试登录

1. 打开应用
2. 点击"使用 Google 登录"
3. 选择你的 Google 账号
4. 授权应用访问 Drive 和 Gmail
5. 查看文件和邮件

### 6.3 常见错误

**错误 1**：`无法连接到 Google 服务`
- **原因**：SHA-1 配置错误
- **解决**：检查 SHA-1 是否正确，包名是否为 `com.claw.api`

**错误 2**：`此应用未经过验证`
- **原因**：应用未发布
- **解决**：点击"高级" > "转到 Claw API（不安全）"继续

**错误 3**：`您无权访问此应用`
- **原因**：邮箱未添加为测试用户
- **解决**：在 OAuth 同意屏幕中添加你的邮箱

## 第七步：发布应用（可选）

### 7.1 发布 OAuth 同意屏幕

如果要让所有用户（不只是测试用户）使用：

1. 转到 OAuth 同意屏幕
2. 点击"发布应用"
3. 确认发布

⚠️ **注意**：
- 发布后，任何 Google 账号都可以使用
- 如果请求敏感作用域，需要 Google 审核（可能需要几天）

### 7.2 域名验证（可选）

如果你的应用需要访问用户敏感数据，可能需要验证域名：
1. 在 OAuth 同意屏幕中添加已授权网域
2. 完成域名验证流程

## 📝 配置清单

在开始之前，确保你已经：

- [ ] 创建了 Google Cloud 项目
- [ ] 启用了 Google Drive API
- [ ] 启用了 Gmail API
- [ ] 配置了 OAuth 同意屏幕（外部类型）
- [ ] 添加了必要的作用域
- [ ] 添加了测试用户（至少你自己）
- [ ] 获取了 SHA-1 证书指纹
- [ ] 创建了 OAuth 2.0 Client ID（Android 类型）
- [ ] 更新了代码中的 SERVER_CLIENT_ID
- [ ] 测试了应用登录功能

## 🔗 相关链接

- [Google Cloud Console](https://console.cloud.google.com/)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [Google Drive API](https://developers.google.com/drive)
- [Gmail API](https://developers.google.com/gmail)

## 🆘 需要帮助？

如果遇到问题：
1. 查看 [故障排除指南](TROUBLESHOOTING.md)（即将推出）
2. 在 [GitHub Issues](https://github.com/fightmonster/ClawAPI/issues) 提问
3. 查看 [Google 官方文档](https://developers.google.com/)
