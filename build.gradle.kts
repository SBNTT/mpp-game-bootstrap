@file:Suppress("UNUSED_VARIABLE")

import java.util.Base64

plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

repositories {
    mavenCentral()
    githubPackage("SBNTT/mpp-game-glfw")
    githubPackage("SBNTT/mpp-game-vulkan")
    githubPackage("SBNTT/mpp-game-common")
}

val mavenRegistryName: String by project
val mavenRegistryUrl: String by project
val mavenRegistryUsernameEnvVariable: String by project
val mavenRegistryPasswordEnvVariable: String by project

val group: String by project
val version: String by project

val glfwVersion: String by project
val vulkanVersion: String by project
val commonVersion: String by project

project.group = group
project.version = version

kotlin {
    macosX64()
    mingwX64()
    linuxX64()

    sourceSets {
        val apiMain by creating {
            dependencies {
                implementation(kotlin("stdlib"))
                api("me.sbntt.mppgame:vulkan:$vulkanVersion")
                implementation("me.sbntt.mppgame:common:$commonVersion")
            }
        }

        val nativeMain by creating {
            dependsOn(apiMain)
        }

        val desktopMain by creating {
            dependsOn(nativeMain)
            dependencies {
                implementation("me.sbntt.mppgame:glfw:$glfwVersion-vulkan.$vulkanVersion")
            }
        }

        val mingwX64Main by getting {
            dependsOn(desktopMain)
        }

        val macosX64Main by getting {
            dependsOn(desktopMain)
        }

        val linuxX64Main by getting {
            dependsOn(desktopMain)
        }
    }
}

publishing {
    repositories {
        maven {
            name = mavenRegistryName
            url = uri(mavenRegistryUrl)
            credentials {
                username = System.getenv(mavenRegistryUsernameEnvVariable)
                password = System.getenv(mavenRegistryPasswordEnvVariable)
            }
        }
    }
}

tasks {
    val macosHostTargets = arrayOf("ios", "tvos", "watchos", "macos")
    val linuxHostTargets = arrayOf("kotlinmultiplatform", "android", "linux", "wasm", "jvm", "js")
    val windowsHostTargets = arrayOf("mingw")

    val hostSpecificBuild by registering {
        dependsOn(when {
            isMacOsHost() -> tasksFiltering("compile", "", false, *macosHostTargets)
            isLinuxHost() -> tasksFiltering("compile", "", false, *linuxHostTargets)
            isWindowsHost() -> tasksFiltering("compile", "", false, *windowsHostTargets)
            else -> throw RuntimeException("Unsupported host")
        })
    }

    val hostSpecificPublish by registering {
        dependsOn(when {
            isMacOsHost() -> tasksFiltering("publish", "${mavenRegistryName}Repository", false, *macosHostTargets)
            isLinuxHost() -> tasksFiltering("publish", "${mavenRegistryName}Repository", false, *linuxHostTargets)
            isWindowsHost() -> tasksFiltering("publish", "${mavenRegistryName}Repository", false, *windowsHostTargets)
            else -> throw RuntimeException("Unsupported host")
        })
    }
}

fun RepositoryHandler.githubPackage(repository: String) = maven("https://maven.pkg.github.com/$repository") {
    credentials {
        username = "SBNTT-machine-user"
        password = String(Base64.getDecoder().decode("Z2hwX3VoTENMZ2xBa3dmdmdPRjRSRDBodDl6RFNqUGdCOTBjZnBONw=="))
    }
}

fun isWindowsHost() = System.getProperty("os.name").startsWith("windows", ignoreCase = true)
fun isMacOsHost() = System.getProperty("os.name").startsWith("mac os", ignoreCase = true)
fun isLinuxHost() = System.getProperty("os.name").startsWith("linux", ignoreCase = true)

fun tasksFiltering(prefix: String, suffix: String, test: Boolean, vararg platforms: String) = tasks.names
        .asSequence()
        .filter { it.startsWith(prefix, ignoreCase = true) }
        .filter { it.endsWith(suffix, ignoreCase = true) }
        .filter { it.endsWith("test", ignoreCase = true) == test }
        .filter { it.contains("test", ignoreCase = true) == test }
        .filter { task -> platforms.any { task.contains(it, ignoreCase = true) } }
        .toMutableList()
