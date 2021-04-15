package me.sbntt.mppgame.bootstrap

class VulkanConfiguration(
    val engineName: String? = null,
    val engineVersion: GameVersion? = null,
    val validationLayers: Set<String> = setOf(),
    val requiredInstanceExtensions: Set<String> = setOf(),
    val optionalInstanceExtensions: Set<String> = setOf(),
    val requiredDeviceExtensions: Set<String> = setOf(),
    val optionalDeviceExtensions: Set<String> = setOf(),
    val requiredQueues: Set<VulkanQueues> = setOf(),
    val optionalQueues: Set<VulkanQueues> = setOf()
)