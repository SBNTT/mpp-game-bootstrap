package me.sbntt.mppgame.bootstrap.mixins

import kotlinx.cinterop.*
import me.sbntt.mppgame.vulkan.*

internal interface VulkanExtensionsMixin {

    fun getAvailableVulkanInstanceExtensions(): Set<String> = memScoped {
        val count = alloc<UIntVar>()
        if (vkEnumerateInstanceExtensionProperties(null, count.ptr, null) != VK_SUCCESS) {
            throw RuntimeException("Cannot get available instance extensions count")
        }

        val buffer = allocArray<VkExtensionProperties>(count.value.convert())
        if (vkEnumerateInstanceExtensionProperties(null, count.ptr, buffer) != VK_SUCCESS) {
            throw RuntimeException("Cannot get available instance extensions")
        }

        ((0 until count.value.convert()).map {
            buffer[it].extensionName.toKString()
        }).toSet()
    }

    fun VkPhysicalDevice.getAvailableExtensions(): Set<String> = memScoped {
        val count = alloc<UIntVar>()
        if (vkEnumerateDeviceExtensionProperties(this@getAvailableExtensions, null, count.ptr, null) != VK_SUCCESS) {
            throw RuntimeException("Cannot get available device extensions count")
        }

        val buffer = allocArray<VkExtensionProperties>(count.value.convert())
        if (vkEnumerateDeviceExtensionProperties(this@getAvailableExtensions, null, count.ptr, buffer) != VK_SUCCESS) {
            throw RuntimeException("Cannot get available device extensions")
        }

        ((0 until count.value.convert()).map {
            buffer[it].extensionName.toKString()
        }).toSet()
    }

}