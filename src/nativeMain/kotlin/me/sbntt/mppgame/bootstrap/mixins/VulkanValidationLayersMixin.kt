package me.sbntt.mppgame.bootstrap.mixins

import kotlinx.cinterop.*
import me.sbntt.mppgame.vulkan.VK_SUCCESS
import me.sbntt.mppgame.vulkan.VkLayerProperties
import me.sbntt.mppgame.vulkan.vkEnumerateInstanceLayerProperties

internal interface VulkanValidationLayersMixin {

    fun getAvailableVulkanValidationLayers(): Set<String> = memScoped {
        val count = alloc<UIntVar>()
        if (vkEnumerateInstanceLayerProperties(count.ptr, null) != VK_SUCCESS) {
            throw RuntimeException("Cannot get available validation layers count")
        }

        val buffer = allocArray<VkLayerProperties>(count.value.convert())
        if (vkEnumerateInstanceLayerProperties(count.ptr, buffer) != VK_SUCCESS) {
            throw RuntimeException("Cannot get available validation layers")
        }

        ((0 until count.value.convert()).map {
            buffer[it].layerName.toKString()
        }).toSet()
    }

}