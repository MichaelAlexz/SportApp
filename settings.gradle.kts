pluginManagement {
    repositories {
        // 官方源（GitHub Actions 用这些，速度快）
        google()
        mavenCentral()
        gradlePluginPortal()

        // 国内镜像（国内网络故障时的回退）
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 官方源优先
        google()
        mavenCentral()

        // 国内镜像次选
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
    }
}

rootProject.name = "SportApp"
include(":app")
