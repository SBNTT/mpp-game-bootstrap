package me.sbntt.mppgame.bootstrap

class GameConfiguration(
    val name: String,
    val version: GameVersion,
    val window: GameWindowConfiguration,
    val vulkan: VulkanConfiguration = VulkanConfiguration()
)