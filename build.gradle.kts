@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

group = "me.sbntt.mpp.bootstrap"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    macosX64()
    mingwX64(); mingwX86()
    linuxX64();
    androidNativeArm64(); androidNativeArm32(); androidNativeX64(); androidNativeX86()
    iosArm64(); iosArm32(); iosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val mingwX64Main by getting; val mingwX86Main by getting
        val macosX64Main by getting
        val linuxX64Main by getting
        val androidNativeArm64Main by getting; val androidNativeArm32Main by getting;
        val androidNativeX64Main by getting; val androidNativeX86Main by getting;
        val iosArm64Main by getting; val iosArm32Main by getting; val iosX64Main by getting

        val windowsMain by creating {
            mingwX64Main.dependsOn(this)
            mingwX86Main.dependsOn(this)
        }

        val macosMain by creating {
            macosX64Main.dependsOn(this)
        }

        val linuxMain by creating {
            linuxX64Main.dependsOn(this)
        }

        val desktopMain by creating {
            dependsOn(commonMain)
            windowsMain.dependsOn(this)
            macosMain.dependsOn(this)
            linuxMain.dependsOn(this)
        }

        val androidMain by creating {
            dependsOn(commonMain)
            androidNativeArm64Main.dependsOn(this); androidNativeArm32Main.dependsOn(this)
            androidNativeX64Main.dependsOn(this); androidNativeX86Main.dependsOn(this)
        }

        val iosMain by creating {
            dependsOn(commonMain)
            iosArm64Main.dependsOn(this); iosArm32Main.dependsOn(this); iosX64Main.dependsOn(this)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

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
        tasksFiltering("compile", "", false, "ios", "tvos", "watchos", "macos").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val testFromMacos by registering {
        tasksFiltering("", "", true, "ios", "tvos", "watchos", "macos").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val publishFromMacos by registering {
        tasksFiltering("publish", "GitHubPackagesRepository", false, "ios", "tvos", "watchos", "macos").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val buildFromLinux by registering {
        (tasksFiltering("compile", "", false, "android", "linux", "wasm", "js") + "jsJar" + "jvmJar").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val testFromLinux by registering {
        tasksFiltering("", "", true, "android", "linux", "wasm", "js", "jvm").forEach {
            dependsOn(this@tasks.getByName(it))
        }
    }

    val publishFromLinux by registering {
        tasksFiltering("publish", "GitHubPackagesRepository", false, "android", "linux", "wasm", "js", "jvm").forEach {
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


fun tasksFiltering(prefix: String, suffix: String, test: Boolean, vararg platforms: String) = tasks.names
        .asSequence()
        .filter { it.startsWith(prefix, ignoreCase = true) }
        .filter { it.endsWith(suffix, ignoreCase = true) }
        .filter { it.endsWith("test", ignoreCase = true) == test }
        .filter { it.contains("test", ignoreCase = true) == test }
        .filter { task -> platforms.any { task.contains(it, ignoreCase = true) } }
        .toMutableList()