package me.sbntt.mppgame.bootstrap

import kotlinx.cinterop.MemScope
import me.sbntt.mppgame.vulkan.VkInstance
import me.sbntt.mppgame.vulkan.VkDevice
import me.sbntt.mppgame.vulkan.VkQueue

class GameContext {

    lateinit var memScope: MemScope
    lateinit var configuration: GameConfiguration
    lateinit var window: GameWindow
    lateinit var instanceExtensions: Set<String>
    lateinit var deviceExtensions: Set<String>
    lateinit var validationLayers: Set<String>

    lateinit var vulkanInstance: VkInstance
    lateinit var vulkanDevice: VkDevice
    lateinit var vulkanQueues: Map<VulkanQueues, VkQueue?>

}