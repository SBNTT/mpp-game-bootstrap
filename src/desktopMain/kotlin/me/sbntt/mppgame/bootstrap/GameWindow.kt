package me.sbntt.mppgame.bootstrap

import kotlinx.cinterop.*
import me.sbntt.mppgame.glfw.*

actual class GameWindow actual constructor(
    internal actual val configuration: GameWindowConfiguration
) {

    private lateinit var glfwWindow: CPointer<GLFWwindow>

    internal actual fun init() {
        glfwSetErrorCallback(staticCFunction { code, description ->
            println("GLFW error: $code : ${description?.toKString()}")
        })

        if (glfwInit() != GLFW_TRUE) throw RuntimeException("GLFW initialization failed")
        if (glfwVulkanSupported() != GLFW_TRUE) throw RuntimeException("Vulkan is not supported")

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
        glfwWindowHint(GLFW_RESIZABLE, if (isResizable()) GLFW_TRUE else GLFW_FALSE)

        glfwWindow = glfwCreateWindow(
            configuration.width,
            configuration.height,
            configuration.name,
            getMonitor(),
            null
        ) ?: throw RuntimeException("GLFW window creation failed")
    }

    private fun isResizable() = configuration is GameWindowConfiguration.Resizable

    private fun getMonitor(): CPointer<GLFWmonitor>? {
        if (configuration !is GameWindowConfiguration.FullScreen) return null
        return glfwGetPrimaryMonitor()
    }

    internal actual fun getRequiredInstanceExtensions(): List<String> = memScoped {
        val count = alloc<UIntVar>()
        val extensions = glfwGetRequiredInstanceExtensions(count.ptr)
            ?: throw RuntimeException("Get GLFW required instance extensions failed")

        return (0 until count.value.convert()).map { extensions[it]!!.toKString() }
    }

    internal actual fun shouldClose() = glfwWindowShouldClose(glfwWindow) == GLFW_TRUE

    internal actual fun update() {
        glfwPollEvents()
    }

    internal actual fun terminate() {
        if (::glfwWindow.isInitialized) glfwDestroyWindow(glfwWindow)
        glfwTerminate()
    }

}