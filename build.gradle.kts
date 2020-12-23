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
    jvm()

    macosX64()
    mingwX64(); mingwX86()
    linuxX64(); linuxArm64(); linuxArm32Hfp(); linuxMips32(); linuxMipsel32()
    androidNativeArm64(); androidNativeArm32(); androidNativeX64(); androidNativeX86()
    iosArm64(); iosArm32(); iosX64()
    watchosArm64(); watchosArm32()
    tvosArm64(); tvosX64()
    js { browser(); nodejs() }
    wasm32()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
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