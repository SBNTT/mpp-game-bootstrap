package me.sbntt.mppgame.bootstrap.mixins

import kotlinx.cinterop.*
import me.sbntt.mppgame.bootstrap.GameContext
import me.sbntt.mppgame.vulkan.*

internal interface VulkanPhysicalDeviceMixin : VulkanExtensionsMixin {

    @ExperimentalUnsignedTypes
    fun getBestSuitableVulkanPhysicalDevice(memScope: MemScope, context: GameContext): VkPhysicalDevice {
        return getAvailableDevices(memScope, context)
            .filter { it.isSuitable(context) }
            .maxByOrNull { it.getScore() }
            ?: throw RuntimeException("No suitable device found")

    }

    private fun getAvailableDevices(memScope: MemScope, context: GameContext): Set<VkPhysicalDevice> {
        return with(memScope) {
            val count = alloc<UIntVar>()
            if (vkEnumeratePhysicalDevices(context.vulkanInstance, count.ptr, null) != VK_SUCCESS) {
                throw RuntimeException("Cannot get available physical devices count")
            }

            val buffer = allocArray<VkPhysicalDeviceVar>(count.value.convert())
            if (vkEnumeratePhysicalDevices(context.vulkanInstance, count.ptr, buffer) != VK_SUCCESS) {
                throw RuntimeException("Cannot get available physical devices")
            }

            ((0 until count.value.convert()).map {
                buffer[it]
            }).filterNotNull().toSet()
        }
    }

    private fun VkPhysicalDevice.isSuitable(context: GameContext): Boolean {
        return this.getAvailableExtensions().containsAll(context.configuration.vulkan.requiredDeviceExtensions)
    }

    @ExperimentalUnsignedTypes
    private fun VkPhysicalDevice.getScore(): Int = memScoped {
        val properties = alloc<VkPhysicalDeviceProperties>()
        vkGetPhysicalDeviceProperties(this@getScore, properties.ptr)

        val memoryProperties = alloc<VkPhysicalDeviceMemoryProperties>()
        vkGetPhysicalDeviceMemoryProperties(this@getScore, memoryProperties.ptr)

        var score = (0 until memoryProperties.memoryHeapCount.convert())
            .filter { memoryProperties.memoryHeaps[it].flags and VK_MEMORY_HEAP_DEVICE_LOCAL_BIT > 0u }
            .sumOf { memoryProperties.memoryHeaps[it].size }
            .div(1000000.convert())
            .toInt()

        score += properties.limits.maxImageDimension2D.toInt()
        score += properties.limits.maxImageDimension3D.toInt()

        score += when (properties.deviceType) {
            VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU -> 99999999
            VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU -> 99999
            else -> 0
        }

        score
    }

}