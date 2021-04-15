package me.sbntt.mppgame.bootstrap.mixins

import kotlinx.cinterop.*
import me.sbntt.mppgame.bootstrap.GameContext
import me.sbntt.mppgame.vulkan.*
import kotlin.native.concurrent.freeze

internal interface VulkanLogicalDeviceMixin : VulkanPhysicalDeviceMixin, VulkanQueuesMixin {

    @ExperimentalUnsignedTypes
    fun createVulkanLogicalDevice(context: GameContext) = memScoped {
        // use temp MemScope for intermediate create info structs
        val physicalDevice = getBestSuitableVulkanPhysicalDevice(memScope, context)
        val queuesIndices = getVulkanQueuesIndices(memScope, context, physicalDevice)
        val createInfo = getCreateInfo(memScope, context, physicalDevice, queuesIndices.values.filterNotNull())

        // allocate a VkDevice var in the global MemScope
        val deviceVar = context.memScope.alloc<VkDeviceVar>()

        if (vkCreateDevice(physicalDevice, createInfo.ptr, null, deviceVar.ptr) != VK_SUCCESS || deviceVar.value == null) {
            throw RuntimeException("Vulkan logical device creation failed")
        }

        context.vulkanDevice = deviceVar.value!!.freeze()
        context.vulkanQueues = queuesIndices.map { (queue, index) ->
            queue to if(index != null) context.vulkanDevice.getQueue(context, index) else null
        }.toMap().freeze()
    }

    private fun getCreateInfo(memScope: MemScope, context: GameContext, physicalDevice: VkPhysicalDevice, queuesIndices: List<Int>): VkDeviceCreateInfo {
        context.deviceExtensions = getExtensions(context, physicalDevice)

        return with(memScope) {
            alloc {
                sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO
                pQueueCreateInfos = getQueuesCreateInfos(memScope, queuesIndices)
                queueCreateInfoCount = queuesIndices.size.convert()
                pEnabledFeatures = getDeviceFeatures(memScope).ptr
                enabledExtensionCount = context.deviceExtensions.size.convert()
                ppEnabledExtensionNames = context.deviceExtensions.toList().toCStringArray(memScope)
                enabledLayerCount = context.validationLayers.size.convert()
                ppEnabledLayerNames = context.validationLayers.toList().toCStringArray(memScope)
            }
        }
    }

    private fun getExtensions(context: GameContext, physicalDevice: VkPhysicalDevice): Set<String> {
        val requiredExtensions = context.configuration.vulkan.requiredDeviceExtensions
        val availableExtensions = physicalDevice.getAvailableExtensions()

        return requiredExtensions + context.configuration.vulkan.optionalDeviceExtensions.filter {
            availableExtensions.contains(it)
        }
    }

    private fun getQueuesCreateInfos(memScope: MemScope, queuesIndices: List<Int>): CPointer<VkDeviceQueueCreateInfo>  {
        return with(memScope) {
            var index = 0
            allocArray(queuesIndices.size) {
                sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO
                queueFamilyIndex = queuesIndices[index++].convert()
                queueCount = 1.convert()
                pQueuePriorities = floatArrayOf(1f).toCValues().ptr
            }
        }
    }

    private fun getDeviceFeatures(memScope: MemScope): VkPhysicalDeviceFeatures {
        return with(memScope) {
            alloc {

            }
        }
    }

}