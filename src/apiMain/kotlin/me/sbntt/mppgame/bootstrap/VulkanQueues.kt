package me.sbntt.mppgame.bootstrap

import me.sbntt.mppgame.vulkan.VK_QUEUE_COMPUTE_BIT
import me.sbntt.mppgame.vulkan.VK_QUEUE_GRAPHICS_BIT
import me.sbntt.mppgame.vulkan.VK_QUEUE_SPARSE_BINDING_BIT
import me.sbntt.mppgame.vulkan.VK_QUEUE_TRANSFER_BIT

enum class VulkanQueues(internal val flags: UInt) {

    COMPUTE(VK_QUEUE_COMPUTE_BIT),
    GRAPHICS(VK_QUEUE_GRAPHICS_BIT),
    TRANSFER(VK_QUEUE_TRANSFER_BIT),
    SPARSE_BINDING(VK_QUEUE_SPARSE_BINDING_BIT)

}