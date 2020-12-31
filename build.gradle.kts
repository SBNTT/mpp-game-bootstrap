@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

group = "me.sbntt.mpp.bootstrap"
version = "0.2.1"

val glfwVersion = "3.3.2"
val vulkanVersion = "1.2.165"

val nativeLibsDir = buildDir.resolve("nativeLibs")
val downloadsDir = buildDir.resolve("tmp")

val glfwDir = nativeLibsDir.resolve("glfw-$glfwVersion-${System.getProperty("os.name").replace(" ", "-")}")
val vulkanDir = nativeLibsDir.resolve("vulkan-$vulkanVersion-${System.getProperty("os.name").replace(" ", "-")}")

repositories {
    mavenCentral()
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
    val setupVulkan by registering {
        if (vulkanDir.exists()) return@registering

        if (!nativeLibsDir.exists()) nativeLibsDir.mkdirs()
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        println("Downloading Vulkan v.$vulkanVersion headers ...")
        val vulkanArchive = downloadsDir.resolve("vulkan-$vulkanVersion.zip")
        download(
            "https://github.com/KhronosGroup/Vulkan-Headers/archive/v$vulkanVersion.zip",
            vulkanArchive
        )

        println("Expanding Vulkan v.$vulkanVersion headers ...")
        copy {
            from(zipTree(vulkanArchive)) {
                include("Vulkan-*/include/**")
                includeEmptyDirs = false
                eachFile {
                    relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
                }
            }
            into(vulkanDir)
        }
        delete(vulkanArchive)
    }

    val setupGlfw by registering {
        if (glfwDir.exists()) return@registering

        if (!nativeLibsDir.exists()) nativeLibsDir.mkdirs()
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val hostOs: String = System.getProperty("os.name")
        val releaseAsset = when {
            hostOs == "Mac OS X" -> "glfw-$glfwVersion.bin.MACOS.zip"
            hostOs.startsWith("Windows") -> "glfw-$glfwVersion.bin.WIN64.zip"
            hostOs == "Linux" ->"glfw-$glfwVersion.zip"
            else -> throw GradleException("Unsupported host for GLFW")
        }

        println("Downloading $releaseAsset ...")
        val glfwArchive = downloadsDir.resolve("glfw-$glfwVersion.zip")
        download(
            "https://github.com/glfw/glfw/releases/download/$glfwVersion/$releaseAsset",
            glfwArchive
        )

        println("Expanding GLFW v.$glfwVersion ...")
        copy {
            from(zipTree(glfwArchive)) {
                includeEmptyDirs = false
                eachFile {
                    relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
                }
            }
            into(glfwDir)
        }
        delete(glfwArchive)

        if (hostOs == "Linux") {
            println("Building GLFW...")
            
            listOf(
                "cmake .",
                "make",
                "mkdir lib-linux",
                "mv src/libglfw3.a lib-linux"
            ).forEach { println(it.runCommand(glfwDir)) }
        }
    }

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
    androidNativeArm64(); androidNativeX64(); androidNativeX86()
    iosArm64(); iosArm32(); iosX64()

    targets.withType<KotlinNativeTarget>().forEach {
        it.compilations.named("main") {
            cinterops.create("vulkan") {
                tasks.named(interopProcessingTaskName) {
                    dependsOn(tasks.named("setupVulkan"))
                }

                includeDirs(vulkanDir.resolve("include"))
            }
        }
    }

    targets.withType<KotlinNativeTarget>()
        .filter { it.targetName.startsWith("mingw") || it.targetName.startsWith("macos") || it.targetName.startsWith("linux")  }
        .forEach {
            it.compilations.named("main") {
                cinterops.create("glfw") {
                    tasks.named(interopProcessingTaskName) {
                        dependsOn(tasks.named("setupVulkan"))
                        dependsOn(tasks.named("setupGlfw"))
                    }

                    includeDirs(glfwDir.resolve("include"))
                    includeDirs(vulkanDir.resolve("include"))
                }
                kotlinOptions{
                    freeCompilerArgs = listOf(
                        "-include-binary", when {
                            it.targetName.startsWith("macos") -> "$glfwDir/lib-macos/libglfw3.a"
                            it.targetName.startsWith("mingw") -> "$glfwDir/lib-mingw-w64/libglfw3.a"
                            else                    /*linux*/ -> "$glfwDir/lib-linux/libglfw3.a"
                        }
                    )
                }
            }
        }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val desktopMain by creating {
            dependsOn(nativeMain)
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

        val androidNativeArm64Main by getting
        val androidNativeX64Main by getting; val androidNativeX86Main by getting;
        val iosArm64Main by getting; val iosArm32Main by getting; val iosX64Main by getting

        val androidMain by creating {
            dependsOn(nativeMain)
            androidNativeArm64Main.dependsOn(this)
            androidNativeX64Main.dependsOn(this); androidNativeX86Main.dependsOn(this)
        }

        val iosMain by creating {
            dependsOn(nativeMain)
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

fun download(url : String, dest: File) {
    ant.invokeMethod("get", mapOf("src" to url, "dest" to dest))
}

fun tasksFiltering(prefix: String, suffix: String, test: Boolean, vararg platforms: String) = tasks.names
        .asSequence()
        .filter { it.startsWith(prefix, ignoreCase = true) }
        .filter { it.endsWith(suffix, ignoreCase = true) }
        .filter { it.endsWith("test", ignoreCase = true) == test }
        .filter { it.contains("test", ignoreCase = true) == test }
        .filter { task -> platforms.any { task.contains(it, ignoreCase = true) } }
        .toMutableList()

fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}