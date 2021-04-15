package me.sbntt.mppgame.bootstrap

import kotlinx.cinterop.*
import me.sbntt.mppgame.bootstrap.mixins.VulkanInstanceMixin
import me.sbntt.mppgame.bootstrap.mixins.VulkanLogicalDeviceMixin
import me.sbntt.mppgame.vulkan.*

internal open class VulkanApp (
    memScope: MemScope,
    configuration: GameConfiguration
) : VulkanInstanceMixin, VulkanLogicalDeviceMixin {

    protected val context = GameContext()

    init {
        context.memScope = memScope
        context.configuration = configuration
    }

    @ExperimentalUnsignedTypes
    internal fun init() {
        context.window = GameWindow(context.configuration.window)
        context.window.init()
        createVulkanInstance(context)
        createVulkanLogicalDevice(context)
    }

    internal fun terminate() {
        vkDestroyDevice(context.vulkanDevice, null)
        vkDestroyInstance(context.vulkanInstance, null)
        context.window.terminate()
    }

}