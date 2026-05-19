pluginManagement {
    repositories {
        // ========================
        // 国内镜像配置（解决网络问题）
        // ========================
        // 阿里云 Maven 镜像（推荐）
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

        // 华为云 Maven 镜像（备用）
        // maven { url = uri("https://repo.huaweicloud.com/repository/maven") }

        // 腾讯云 Maven 镜像（备用）
        // maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public") }

        // 官方源（如需科学上网可放开）
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

        // 官方源（回退）
        google()
        mavenCentral()
    }
}

rootProject.name = "SportApp"
include(":app")
