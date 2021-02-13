@file:Suppress("UNUSED_VARIABLE")

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/sbntt/mpp-game")
}

plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

group   = "me.sbntt.mpp.bootstrap"
version = "0.2.1"

val glfwVersion   = "3.3.2"
val vulkanVersion = "1.2.169"

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/SBNTT/mpp-game-bootstrap")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks {
    val buildFromMacos by registering {
        tasksFiltering("compile", "", false, "ios", "macos").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val testFromMacos by registering {
        tasksFiltering("", "", true, "ios", "macos").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val publishFromMacos by registering {
        tasksFiltering("publish", "GitHubPackagesRepository", false, "ios", "macos").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val buildFromLinux by registering {
        (tasksFiltering("compile", "", false, "android", "linux")).forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val testFromLinux by registering {
        tasksFiltering("", "", true, "android", "linux").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val publishFromLinux by registering {
        tasksFiltering("publish", "GitHubPackagesRepository", false, "android", "linux").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val buildFromWindows by registering {
        tasksFiltering("compile", "", false, "mingw").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val testFromWindows by registering {
        tasksFiltering("", "", true, "mingw").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val publishFromWindows by registering {
        tasksFiltering("publish", "GitHubPackagesRepository", false, "mingw").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }
}

kotlin {
    macosX64()
    mingwX64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("me.sbntt.mppgame:vulkan:$vulkanVersion")
            }
        }

        val mingwX64Main by getting {
            dependsOn(nativeMain)
            dependencies {
                implementation("me.sbntt.mppgame:glfw:$glfwVersion")
            }
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
            dependencies {
                implementation("me.sbntt.mppgame:glfw:$glfwVersion")
            }
        }

        val linuxX64Main by getting {
            dependsOn(nativeMain)
            dependencies {
                implementation("me.sbntt.mppgame:glfw:$glfwVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

fun tasksFiltering(prefix: String, suffix: String, test: Boolean, vararg platforms: String) = tasks.names
        .asSequence()
        .filter { it.startsWith(prefix, ignoreCase = true) }
        .filter { it.endsWith(suffix, ignoreCase = true) }
        .filter { it.endsWith("test", ignoreCase = true) == test }
        .filter { it.contains("test", ignoreCase = true) == test }
        .filter { task -> platforms.any { task.contains(it, ignoreCase = true) } }
        .toMutableList()
