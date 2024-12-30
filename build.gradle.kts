plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.song"
version = "1.0-SNAPSHOT"

repositories {
    // mavenCentral()
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/gradle-plugin")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/jcenter")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.1.4")
    type.set("IC") // Target IDE Platform
    downloadSources.set(false)  // 不下载源码
    updateSinceUntilBuild.set(false)  // 不更新 plugin.xml 的 since/until build 信息
    
    // 只下载必要的插件依赖
    plugins.set(listOf())
    
    // 如果只是构建插件包，可以使用本地已安装的 IDE
    // localPath.set("路径到你本地的 IntelliJ IDEA")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    buildSearchableOptions {
        enabled = false  // 禁用搜索选项构建，减少构建时间
    }
}

dependencies {
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("org.slf4j:slf4j-simple:1.7.32")
}
