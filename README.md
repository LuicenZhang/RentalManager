# 租房管家 · Rental Manager

给个人房东用的租房管理 App：租户、账单、收据，一个应用管完。

Android 原生，Kotlin + Jetpack Compose，界面走 Liquid Glass 玻璃拟态风格。

![version](https://img.shields.io/badge/version-1.2.0-blue)
![minSdk](https://img.shields.io/badge/minSdk-28-green)
![targetSdk](https://img.shields.io/badge/targetSdk-36-green)

---

## 1.2.0 更新了什么

这一版基本是一次界面重做，底层工具链也跟着升了。

**新增 Liquid Glass 组件**：六个，共 946 行

| 组件 | 用途 |
|---|---|
| `LiquidGlassButton` | 带折射感的按钮 |
| `LiquidGlassCard` | 玻璃卡片容器 |
| `LiquidGlassSegmentedTabs` | 玻璃分段选择器 |
| `GlassCenterSheet` | 居中弹出面板 |
| `GlassTopAppBar` | 玻璃顶栏 |
| `TopGradientOverlay` | 顶部渐变遮罩 |

**工具链升级**（这一步是前置的，不升上不去）

| | 原版本 | 新版本 |
|---|---|---|
| Kotlin | 1.9.0 | **2.3.0** |
| AGP | 8.4.1 | **8.6.0** |
| Room | 2.6.1 | **2.8.4** |
| Compose BOM | 2024.05.00 | **2024.11.00** |

Compose 编译器插件从 `composeOptions.kotlinCompilerExtensionVersion` 换成了 Kotlin 官方的 `org.jetbrains.kotlin.plugin.compose`，`jvmTarget` 也移进了 `kotlin { compilerOptions }`。

**重写过的界面**：添加账单、数据迁移、主页、账单列表、添加租户、账单详情、编辑租户、设置、收据、关于，以及两个月份选择器。

**新依赖**：`io.github.kyant0:backdrop:1.0.4`（Maven Central，磨砂背景）。

---

## 功能

**租户管理** — 添加、编辑、查看。房间号、姓名、租金、联系方式。

**账单生成** — 按月给每个租户建账单，水电费按表读数算，支持自定义计量表名称。

**收据** — 生成收据图片，可直接分享给租户，也可保存到相册。

**数据备份** — 导出成文件，换手机时导回来。

**隐私保护** — 收据上可以打码，敏感信息不外泄。

**深色模式** — 跟随系统。

---

## 安装

### 用 APK

从 [Releases](https://github.com/LuicenZhang/RentalManager/releases) 下载最新的 `app-release.apk`，传到手机上点开安装。

首次安装需要允许「安装未知来源应用」。

### 从源码构建

```bash
git clone https://github.com/LuicenZhang/RentalManager.git
cd RentalManager
./gradlew assembleDebug
```

产物在 `app/build/outputs/apk/debug/`。

> Gradle 需要 JDK 17 以上。`local.properties` 里的 `sdk.dir` 要改成你自己机器上的 Android SDK 路径，这个文件不进版本库。

---

## 技术栈

- **语言** Kotlin 2.3.0
- **UI** Jetpack Compose（Material 3）+ Liquid Glass 组件
- **架构** MVVM
- **数据库** Room 2.8.4（KSP 注解处理）
- **异步** Coroutines + Flow
- **导航** Navigation Compose
- **图片** Coil
- **JSON** Gson
- **磨砂** kyant0/backdrop

依赖注入是手写的，没上 Hilt —— 这个体量的应用，手写更省事。

---

## 环境要求

| | |
|---|---|
| Android | 9.0（API 28）及以上 |
| 编译 SDK | 36 |
| 内存 | 2GB 够用，4GB 更顺 |
| 存储 | 约 50MB |

---

## 使用

**第一次用**：点左下角 `+` → 添加租户 → 填房间号、姓名、租金 → 保存。

**建账单**：主页或账单页 → 添加账单 → 选租户和月份 → 填水电表读数 → 保存。

**出收据**：主页租户卡片上点「收据」→ 自动生成当月收据 → 点分享。

**备份**：菜单 → 备份与恢复 → 导出数据。

---

## 项目结构

```
app/src/main/java/com/morgen/rentalmanager/
├── myapplication/          数据层：Room 实体、DAO、Repository
│   └── miniprogram/        小程序数据互通（解析、校验）
├── ui/
│   ├── components/         通用组件 + Liquid Glass 六件套
│   ├── billlist/           账单列表、图表、月份选择
│   ├── billdetail/         账单详情
│   ├── addbillall/         批量添加账单
│   ├── addtenant/          添加租户
│   ├── edittenant/         编辑租户
│   ├── datatransfer/       导入导出
│   ├── receipt/            收据生成
│   ├── setting/            设置
│   ├── about/              关于
│   └── theme/              主题、配色、字体、设计系统
└── utils/                  格式化、导出、备份、隐私打码、性能
```

---

## 关于代码质量

这个项目是和 Claude、ChatGPT、Gemini 一起写的，几家模型的风格混在一起，命名和注释不一致，有些地方绕了远路。能用，但别当范本读。

发现问题欢迎开 issue。

---

## 联系

ikic@qq.com

## 许可

未指定许可证。使用前请先联系作者。