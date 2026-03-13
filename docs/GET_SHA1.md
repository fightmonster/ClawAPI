# 获取 SHA-1 证书指纹指南

## 为什么需要 SHA-1？

Google OAuth 2.0 需要 SHA-1 证书指纹来验证 Android 应用身份。

## 方法 1：使用 Debug 签名（快速测试）

### 前提条件
- 已安装 Android Studio
- 已配置 Android SDK

### 步骤

```bash
# macOS/Linux
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Windows
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

输出示例：
```
SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
```

复制这个 SHA-1 值。

## 方法 2：创建 Release 签名（推荐用于发布）

### 步骤 1：生成密钥库

```bash
keytool -genkey -v -keystore claw-api.keystore -alias claw-api -keyalg RSA -keysize 2048 -validity 10000
```

按提示输入：
- 密钥库密码
- 密钥密码
- 姓名、组织等信息

### 步骤 2：获取 SHA-1

```bash
keytool -list -v -keystore claw-api.keystore -alias claw-api
```

输入密钥库密码后，会显示 SHA-1。

## 方法 3：从 APK 获取（已有 APK）

如果你已经有签名的 APK：

```bash
# 解压 APK
unzip app-release.apk -d app-release

# 从签名文件获取 SHA-1
keytool -printcert -file app-release/META-INF/CERT.RSA
```

## 配置到 Google Cloud Console

1. 复制 SHA-1 值（包括 `SHA1:` 前缀）
2. 转到 Google Cloud Console > API 和服务 > 凭据
3. 编辑你的 OAuth 2.0 Client ID
4. 在"SHA-1 证书指纹"字段粘贴值
5. 保存

## 常见问题

### Q: Debug 和 Release 的 SHA-1 不同吗？
A: 是的，每个签名证书的 SHA-1 都不同。你需要分别配置。

### Q: 多个开发者如何协作？
A: 两种方案：
1. 每个开发者添加自己的 debug SHA-1（推荐）
2. 共享同一个 debug.keystore（不推荐）

### Q: 提示"keytool 命令未找到"
A: 确保已安装 JDK，并将 `$JAVA_HOME/bin` 添加到 PATH。

### Q: 如何验证配置是否正确？
A: 运行应用，尝试 Google 登录。如果配置错误，会提示"无法连接到 Google 服务"。

## 安全提示

⚠️ **不要提交以下文件到 Git**：
- `.keystore` 文件
- `keystore.properties` 文件
- 包含密码的任何文件

这些文件已添加到 `.gitignore`。
