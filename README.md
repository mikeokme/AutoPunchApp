# Auto Punch-in App (自动打卡应用) / 自動出勤アプリ

[![Android](https://img.shields.io/badge/Android-API%2021+-green.svg)](https://developer.android.com/about/versions/android-5.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.14.2-orange.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📱 项目简介 / Project Description / プロジェクト概要

### 🇨🇳 中文
自动打卡应用是一款基于Android无障碍服务的智能自动化平台，支持多种主流应用的自动化操作。通过录制用户的操作轨迹或编写JavaScript脚本，应用可以在指定时间段内自动执行各种操作，帮助用户实现自动化任务。

**主要功能：**
- 🎯 支持多种应用自动化（钉钉、企业微信、微信等）
- ⏰ 智能时间设置，支持随机间隔执行
- 📝 操作轨迹录制与回放
- 🔧 JavaScript脚本编辑器，支持自定义自动化脚本
- 📚 脚本市场，分享和下载自动化脚本
- 🔔 智能提醒与通知
- 📊 执行记录与统计
- ⚙️ 个性化设置

### 🇺🇸 English
Auto Punch-in App is an intelligent automation platform based on Android Accessibility Service, supporting automated operations for various mainstream applications. By recording user operation trajectories or writing JavaScript scripts, the app can automatically execute various operations within specified time periods, helping users achieve automation tasks.

**Key Features:**
- 🎯 Support for multiple app automation (DingTalk, WeChat Work, WeChat, etc.)
- ⏰ Smart time settings with random interval execution
- 📝 Operation trajectory recording and playback
- 🔧 JavaScript script editor supporting custom automation scripts
- 📚 Script marketplace for sharing and downloading automation scripts
- 🔔 Smart reminders and notifications
- 📊 Execution records and statistics
- ⚙️ Personalized settings

### 🇯🇵 日本語
自動出勤アプリは、Androidアクセシビリティサービスをベースにしたインテリジェントな自動化プラットフォームで、様々な主流アプリの自動操作をサポートしています。ユーザーの操作軌跡を記録したり、JavaScriptスクリプトを書いたりすることで、指定された時間帯内で自動的に様々な操作を実行し、ユーザーの自動化タスクを実現します。

**主な機能：**
- 🎯 複数のアプリ自動化をサポート（DingTalk、WeChat Work、WeChatなど）
- ⏰ ランダム間隔実行をサポートするスマート時間設定
- 📝 操作軌跡の記録と再生
- 🔧 カスタム自動化スクリプトをサポートするJavaScriptスクリプトエディタ
- 📚 自動化スクリプトの共有とダウンロードのためのスクリプト市場
- 🔔 スマートリマインダーと通知
- 📊 実行記録と統計
- ⚙️ パーソナライズ設定

---

## 🚀 快速开始 / Quick Start / クイックスタート

### 🇨🇳 中文

#### 环境要求
- Android Studio Arctic Fox 或更高版本
- Android SDK API 21+
- Kotlin 1.9.0+
- Gradle 8.14.2+

#### 安装步骤
1. 克隆项目
```bash
git clone https://github.com/yourusername/auto-punch-app.git
cd auto-punch-app
```

2. 打开Android Studio，导入项目

3. 构建项目
```bash
# Windows用户推荐使用提供的批处理脚本
build.bat

# 或手动构建
./gradlew assembleDebug
```

4. 安装APK到设备
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

5. 开启无障碍服务
   - 进入应用设置
   - 点击"开启无障碍服务"
   - 在系统设置中启用应用的无障碍服务

### 🇺🇸 English

#### Requirements
- Android Studio Arctic Fox or higher
- Android SDK API 21+
- Kotlin 1.9.0+
- Gradle 8.14.2+

#### Installation Steps
1. Clone the project
```bash
git clone https://github.com/yourusername/auto-punch-app.git
cd auto-punch-app
```

2. Open Android Studio and import the project

3. Build the project
```bash
# Windows users are recommended to use the provided batch script
build.bat

# Or build manually
./gradlew assembleDebug
```

4. Install APK to device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

5. Enable Accessibility Service
   - Go to app settings
   - Click "Enable Accessibility Service"
   - Enable the app's accessibility service in system settings

### 🇯🇵 日本語

#### 要件
- Android Studio Arctic Fox以上
- Android SDK API 21+
- Kotlin 1.9.0+
- Gradle 8.14.2+

#### インストール手順
1. プロジェクトをクローン
```bash
git clone https://github.com/yourusername/auto-punch-app.git
cd auto-punch-app
```

2. Android Studioを開いてプロジェクトをインポート

3. プロジェクトをビルド
```bash
# Windowsユーザーは提供されたバッチスクリプトの使用を推奨
build.bat

# または手動でビルド
./gradlew assembleDebug
```

4. デバイスにAPKをインストール
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

5. アクセシビリティサービスを有効化
   - アプリ設定に移動
   - 「アクセシビリティサービスを有効化」をクリック
   - システム設定でアプリのアクセシビリティサービスを有効化

---

## 📖 使用说明 / Usage Guide / 使用方法

### 🇨🇳 中文

#### 首次设置
1. **选择打卡应用**：在应用列表中选择您使用的打卡应用
2. **录制操作**：在目标应用中录制您的打卡操作流程
3. **设置时间**：配置打卡时间段和随机间隔
4. **开启服务**：确保无障碍服务已启用

#### 日常使用
- 应用会在指定时间段内自动执行打卡
- 支持随机间隔，避免被检测为机器人
- 可在设置中查看打卡记录和统计

### 🇺🇸 English

#### Initial Setup
1. **Select Punch-in App**: Choose your punch-in app from the app list
2. **Record Operations**: Record your punch-in operation flow in the target app
3. **Set Time**: Configure punch-in time period and random intervals
4. **Enable Service**: Ensure accessibility service is enabled

#### Daily Usage
- The app will automatically execute punch-in within the specified time period
- Supports random intervals to avoid being detected as a bot
- View punch-in records and statistics in settings

### 🇯🇵 日本語

#### 初期設定
1. **出勤アプリを選択**：アプリリストから使用する出勤アプリを選択
2. **操作を記録**：ターゲットアプリで出勤操作フローを記録
3. **時間を設定**：出勤時間帯とランダム間隔を設定
4. **サービスを有効化**：アクセシビリティサービスが有効になっていることを確認

#### 日常使用
- アプリは指定された時間帯内で自動的に出勤を実行
- ボットとして検出されることを避けるためランダム間隔をサポート
- 設定で出勤記録と統計を確認可能

---

## 🏗️ 项目结构 / Project Structure / プロジェクト構造

```
app/
├── src/main/
│   ├── java/com/example/autopunchapp/
│   │   ├── model/           # 数据模型 / Data Models / データモデル
│   │   ├── service/         # 服务层 / Services / サービス層
│   │   ├── ui/              # 用户界面 / UI Components / UIコンポーネント
│   │   │   ├── home/        # 主页 / Home / ホーム
│   │   │   ├── appselection/ # 应用选择 / App Selection / アプリ選択
│   │   │   ├── recordaction/ # 操作录制 / Action Recording / 操作記録
│   │   │   ├── timesetting/ # 时间设置 / Time Setting / 時間設定
│   │   │   ├── punchplan/   # 打卡计划 / Punch Plan / 出勤計画
│   │   │   ├── settings/    # 设置 / Settings / 設定
│   │   │   └── notification/ # 通知 / Notification / 通知
│   │   └── utils/           # 工具类 / Utilities / ユーティリティ
│   └── res/                 # 资源文件 / Resources / リソース
└── build.gradle            # 构建配置 / Build Configuration / ビルド設定
```

---

## 🔧 技术栈 / Tech Stack / 技術スタック

### 🇨🇳 中文
- **开发语言**: Kotlin
- **UI框架**: Android Jetpack Compose + ViewBinding
- **架构模式**: MVVM
- **数据存储**: SharedPreferences + Gson
- **无障碍服务**: AccessibilityService
- **构建工具**: Gradle

### 🇺🇸 English
- **Language**: Kotlin
- **UI Framework**: Android Jetpack Compose + ViewBinding
- **Architecture**: MVVM
- **Data Storage**: SharedPreferences + Gson
- **Accessibility**: AccessibilityService
- **Build Tool**: Gradle

### 🇯🇵 日本語
- **開発言語**: Kotlin
- **UIフレームワーク**: Android Jetpack Compose + ViewBinding
- **アーキテクチャ**: MVVM
- **データストレージ**: SharedPreferences + Gson
- **アクセシビリティ**: AccessibilityService
- **ビルドツール**: Gradle

---

## 🛠️ 开发环境 / Development Environment / 開発環境

### 🇨🇳 中文

#### 解决构建问题
如果遇到文件锁定问题，请使用提供的批处理脚本：
```bash
build.bat
```

或手动执行：
```bash
./gradlew --stop
taskkill /F /IM java.exe
taskkill /F /IM studio64.exe
./gradlew assembleDebug --no-daemon
```

### 🇺🇸 English

#### Troubleshooting Build Issues
If you encounter file locking issues, use the provided batch script:
```bash
build.bat
```

Or execute manually:
```bash
./gradlew --stop
taskkill /F /IM java.exe
taskkill /F /IM studio64.exe
./gradlew assembleDebug --no-daemon
```

### 🇯🇵 日本語

#### ビルド問題の解決
ファイルロックの問題が発生した場合は、提供されたバッチスクリプトを使用してください：
```bash
build.bat
```

または手動で実行：
```bash
./gradlew --stop
taskkill /F /IM java.exe
taskkill /F /IM studio64.exe
./gradlew assembleDebug --no-daemon
```

---

## 📄 许可证 / License / ライセンス

### 🇨🇳 中文
本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

### 🇺🇸 English
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### 🇯🇵 日本語
このプロジェクトはMITライセンスの下でライセンスされています - 詳細は[LICENSE](LICENSE)ファイルを参照してください。

---

## 🤝 贡献 / Contributing / 貢献

### 🇨🇳 中文
欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

### 🇺🇸 English
Issues and Pull Requests are welcome!

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 🇯🇵 日本語
IssueとPull Requestを歓迎します！

1. プロジェクトをフォーク
2. 機能ブランチを作成 (`git checkout -b feature/AmazingFeature`)
3. 変更をコミット (`git commit -m 'Add some AmazingFeature'`)
4. ブランチにプッシュ (`git push origin feature/AmazingFeature`)
5. Pull Requestを開く

---

## 📞 联系方式 / Contact / 連絡先

### 🇨🇳 中文
- 邮箱: yangben897251@gmail.com
- 项目链接: [https://github.com/yourusername/auto-punch-app](https://github.com/yourusername/auto-punch-app)

### 🇺🇸 English
- Email: yangben897251@gmail.com
- Project Link: [https://github.com/yourusername/auto-punch-app](https://github.com/yourusername/auto-punch-app)

### 🇯🇵 日本語
- メール: yangben897251@gmail.com
- プロジェクトリンク: [https://github.com/yourusername/auto-punch-app](https://github.com/yourusername/auto-punch-app)

---

## ⚠️ 免责声明 / Disclaimer / 免責事項

### 🇨🇳 中文
本应用仅供学习和研究使用。使用者应遵守相关法律法规和公司规定，不得用于非法用途。开发者不对使用本应用造成的任何后果承担责任。

### 🇺🇸 English
This application is for learning and research purposes only. Users should comply with relevant laws, regulations, and company policies, and should not use it for illegal purposes. The developers are not responsible for any consequences resulting from the use of this application.

### 🇯🇵 日本語
このアプリケーションは学習と研究目的のみです。ユーザーは関連する法律、規制、会社のポリシーに従い、違法な目的で使用してはなりません。開発者はこのアプリケーションの使用による結果について責任を負いません。

---

<div align="center">

**⭐ 如果这个项目对您有帮助，请给它一个星标！ / If this project helps you, please give it a star! / このプロジェクトが役に立った場合は、スターを付けてください！ ⭐**

</div> 
