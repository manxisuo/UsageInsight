pluginManagement {
    repositories {
        google() // 确保包含 Google 仓库
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // 需要添加回来，因为 MPAndroidChart 在这里
    }
}

rootProject.name = "UsageInsight"
include(":app")
