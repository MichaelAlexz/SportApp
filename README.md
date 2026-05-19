# 轻动 - 运动记录 APP

一款极简风格的运动记录 Android APP，复刻 Keep/悦跑圈/咕咚视觉风格。

---

## 🚀 三种构建方式

### 方式一：GitHub Actions 云端编译（最简单，推荐）

**无需安装任何东西，直接在云端生成 APK 下载即可。**

1. 将代码推送到你的 GitHub 仓库：
   ```
   git init
   git add .
   git commit -m "init"
   git remote add origin https://github.com/你的用户名/SportApp.git
   git push -u origin main
   ```

2. 打开 GitHub → Actions 页面 → 选择 **"Build APK"** → 点 **"Run workflow"**
3. 等几分钟，构建完成后点进 workflow → 在 **Artifacts** 区域下载 APK

> 💡 免费额度：GitHub Actions 每月有 2000 分钟免费构建时长，完全够用。

---

### 方式二：本地编译（使用国内镜像）

如果你本地有 Android Studio 但网络慢：

**第1步：确保 gradle-wrapper.jar 存在**
```
如果你下载的是完整项目但缺少 gradlew + gradle-wrapper.jar：
→ 打开 Android Studio，用 Open 打开项目文件夹，Studio 会自动下载 wrapper
→ 或者从其他 Android 项目复制 gradlew、gradlew.bat、gradle/wrapper/ 目录过来
```

**第2步：直接双击 `build.bat` 运行构建脚本**
```
# 或者在命令行：
cd SportApp
gradlew assembleDebug
```

**镜像配置说明：**
| 配置项 | 镜像地址 |
|--------|---------|
| Gradle 下载 | `mirrors.cloud.tencent.com/gradle/` |
| Maven 依赖 | `maven.aliyun.com/repository/` |
| Google 仓库 | `maven.aliyun.com/repository/google` |

这些已经在 `settings.gradle.kts` 和 `gradle-wrapper.properties` 中配置好，直接使用即可。

---

### 方式三：腾讯云 DevOps / 阿里云云效（国内云构建）

比 GitHub Actions 更适合国内网络：

- **腾讯云 Coding.net** → 免费构建池，国内服务器极速下载
- **阿里云云效 (devops.aliyun.com)** → 免费 1800 分钟/月

配置方式类似 GitHub Actions，只需导入项目 → 选择 Android 模板 → 一键构建。

---

## 📱 项目结构

```
SportApp/
├── .github/workflows/    ← GitHub Actions 云端编译配置
├── app/
│   ├── src/main/java/com/sportapp/
│   │   ├── data/          ← Room 数据库 (实体/DAO/Repository)
│   │   ├── service/       ← 后台服务 (计步器/GPS追踪/提醒)
│   │   ├── ui/            ← Compose UI 界面
│   │   ├── util/          ← 工具类 (卡路里计算)
│   │   └── viewmodel/     ← ViewModel 数据层
│   └── build.gradle.kts   ← 应用构建配置
├── settings.gradle.kts    ← 项目设置 (已配阿里云镜像)
└── build.bat              ← Windows 构建脚本
```

## 🔧 最低要求

- Android 8.0 (API 26) 及以上
- GPS + 计步传感器（大部分国产手机都有）
- 约 50MB 安装包体积
