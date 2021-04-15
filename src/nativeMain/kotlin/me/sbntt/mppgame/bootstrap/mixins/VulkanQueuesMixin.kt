package me.sbntt.mppgame.bootstrap.mixins

import kotlinx.cinterop.*
import me.sbntt.mppgame.bootstrap.GameContext
import me.sbntt.mppgame.bootstrap.VulkanQueues
import me.sbntt.mppgame.vulkan.*

internal interface VulkanQueuesMixin {

    @ExperimentalUnsignedTypes
    fun getVulkanQueuesIndices(memScope: MemScope, context: GameContext, device: VkPhysicalDevice): Map<VulkanQueues, Int?> {
        val queueFamilyProperties = device.getQueueFamilyProperties(memScope)

        val queues = context.configuration.vulkan.requiredQueues + context.configuration.vulkan.optionalQueues

        val suitableQueuesIndices = queues
            .map { queue ->
                queue to queueFamilyProperties.withIndex()
                    .filter { it.value.queueFlags and queue.flags == queue.flags }
                    .map { it.index }
            }

        val queuesIndices = queues
            .map { it to null as Int? }
            .toMap().toMutableMap()

        val queuesIndicesCount = (queueFamilyProperties.indices)
            .map { it to 0u }
            .toMap().toMutableMap()

        suitableQueuesIndices
            .sortedBy { it.second.size }
            .forEach { (queue, indices) ->
                indices.firstOrNull { suitableIndex ->
                    queuesIndicesCount[suitableIndex]!! < queueFamilyProperties[suitableIndex].queueCount
                }?.let { index ->
                    queuesIndices[queue] = index
                    queuesIndicesCount[index] = queuesIndicesCount[index]!! + 1u
                }
            }

        if (queuesIndices.any { it.value == null && context.configuration.vulkan.requiredQueues.contains(it.key) }) {
            throw RuntimeException("Some required queues are not available")
        }

        return queuesIndices
    }

    fun VkDevice.getQueue(context: GameContext, queueFamilyIndex: Int): VkQueue {
        return with(context.memScope) {
            val queueVar = alloc<VkQueueVar>()
            vkGetDeviceQueue(this@getQueue, queueFamilyIndex.convert(), 0.convert(), queueVar.ptr)
            queueVar.value ?: throw RuntimeException("Fail to get queue $queueFamilyIndex")
        }
    }

    private fun VkPhysicalDevice.getQueueFamilyProperties(memScope: MemScope): List<VkQueueFamilyProperties> {
        return with(memScope) {
            val count = alloc<UIntVar>()
            vkGetPhysicalDeviceQueueFamilyProperties(this@getQueueFamilyProperties, count.ptr, null)

            val buffer = allocArray<VkQueueFamilyProperties>(count.value.convert())
            vkGetPhysicalDeviceQueueFamilyProperties(this@getQueueFamilyProperties, count.ptr, buffer)

            ((0 until count.value.convert()).map {
                buffer[it]
            }).toList()
        }
    }

}