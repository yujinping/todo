# Todo 任务管理

Kotlin Multiplatform (KMP) + Compose Multiplatform 全日历任务管理应用。

支持 **日 / 周 / 月 / 日历** 四视图，跨平台部署 **Android / iOS / macOS / Windows**。

后端采用 **Ktor + H2** 内嵌数据库，零部署依赖。

---

## 架构

```
app/
├── androidApp/    # Android 入口 (MainActivity)
├── desktopApp/    # Desktop 入口 (JVM/Compose Multiplatform)
├── iosApp/        # iOS Xcode 项目 + SwiftUI 桥接
└── shared/        # Compose Multiplatform 共享 UI + ViewModel
core/              # 领域模型 + Repository 接口 + 工具类
server/            # Ktor 服务端 + H2 REST API
```

## 核心功能

- **日视图**: 今日任务列表，快速添加/完成/删除
- **周视图**: 7 天横向日历 + 选中日任务详情
- **月视图**: 日历网格，红点标记未完成任务，绿点标记全部完成
- **优先级**: 高(红) / 中(橙) / 低(绿) 三级

## 运行

```bash
# 设置 Java 21
export JAVA_HOME=/path/to/jdk-21

# 桌面应用
./gradlew :app:desktopApp:run

# 服务端
./gradlew :server:run

# Android
./gradlew :app:androidApp:assembleDebug

# iOS — 用 Xcode 打开 app/iosApp
```

## 测试

```bash
./gradlew :core:jvmTest :app:shared:jvmTest :server:test
```

## 技术栈

| 层级 | 技术 |
|------|------|
| UI | Compose Multiplatform + Material3 |
| 客户端状态 | ViewModel + StateFlow |
| 序列化 | kotlinx.serialization |
| 日期 | kotlinx-datetime |
| 服务端 | Ktor 3.5 (Netty) |
| 数据库 | H2 嵌入式 |
| 构建 | Gradle 9.1 + Kotlin 2.4 |
