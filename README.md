# UsageInsight

一个基于Android使用数据分析的个性化报告生成器。

## 功能特点

- 📱 应用使用情况跟踪
  - Top 10应用使用时长统计
  - 使用时间段分析
  - 使用趋势可视化

- 🔓 解锁行为分析
  - 解锁次数统计
  - 高频解锁时段识别

- 📬 通知管理洞察
  - 通知数量统计
  - 应用通知分布

- 🤖 AI驱动的个性化建议
  - 基于DeepSeek的智能分析
  - 个性化使用建议

## 技术栈

- 🏗️ 架构：MVVM + Clean Architecture
- 🎨 UI：Jetpack Compose
- 💾 存储：Room + DataStore
- 📊 可视化：MPAndroidChart
- 🌐 网络：Retrofit + Kotlin Coroutines
- 🤖 AI：DeepSeek API

## 开发环境要求

- Android Studio Hedgehog | 2023.1.1
- Kotlin 1.9.22
- Minimum SDK: 29 (Android 10.0)
- Target SDK: 34 (Android 14)

## 使用说明

1. 克隆项目
```bash
git clone git@github.com:manxisuo/UsageInsight.git
```

2. 在Android Studio中打开项目

3. 运行应用前需要：
   - 授予使用情况访问权限
   - 授予通知访问权限
   - 配置DeepSeek API Key（可选）

## 许可证

[MIT License](LICENSE)
