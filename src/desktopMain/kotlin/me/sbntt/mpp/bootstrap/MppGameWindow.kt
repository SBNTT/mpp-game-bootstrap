package me.sbntt.mpp.bootstrap

import glfw.*
import kotlinx.cinterop.CPointer

actual class MppGameWindow actual constructor(
    actual val name: String,
    actual val width: Int,
    actual val height: Int
) {

    private lateinit var glfwWindow: CPointer<GLFWwindow>

    actual fun init() {
        if (glfwInit() != GLFW_TRUE) throw RuntimeException("GLFW initialization fail")
        if (glfwVulkanSupported() != GLFW_TRUE) throw RuntimeException("Vulkan is not supported")

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

        glfwWindow = glfwCreateWindow(width, height, name, null, null)
            ?: throw RuntimeException("GLFW window creation fail")
    }

    actual fun shouldClose() = glfwWindowShouldClose(glfwWindow) == GLFW_TRUE

    actual fun update() {
        glfwPollEvents()
    }

    actual fun terminate() {
        if (::glfwWindow.isInitialized) glfwDestroyWindow(glfwWindow)
        glfwTerminate()
    }

}