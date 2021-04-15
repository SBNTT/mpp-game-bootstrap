package me.sbntt.mppgame.bootstrap.mixins

import kotlinx.cinterop.*
import me.sbntt.mppgame.bootstrap.GameContext
import me.sbntt.mppgame.vulkan.*
import kotlin.native.concurrent.freeze

internal interface VulkanInstanceMixin : VulkanExtensionsMixin, VulkanValidationLayersMixin {

    fun createVulkanInstance(context: GameContext) = memScoped {
        // use temp MemScope for appInfo and createInfo
        val appInfo = getAppInfo(memScope, context)
        val createInfo = getCreateInfo(memScope, appInfo, context)

        // allocate a VkInstance var in the global MemScope
        val instanceVar = context.memScope.alloc<VkInstanceVar>()

        if (vkCreateInstance(createInfo.ptr, null, instanceVar.ptr) != VK_SUCCESS || instanceVar.value == null) {
            throw RuntimeException("Vulkan instance creation failed")
        }

        context.vulkanInstance = instanceVar.value!!.freeze()
    }

    private fun getAppInfo(memScope: MemScope, context: GameContext): VkApplicationInfo {
        return with(memScope) {
            alloc {
                sType = VK_STRUCTURE_TYPE_APPLICATION_INFO
                apiVersion = VK_API_VERSION_1_2
                pApplicationName = context.configuration.name.cstr.ptr
                applicationVersion = with(context.configuration.version) {
                    vkMakeVersion(major.convert(), minor.convert(), patch.convert())
                }
                context.configuration.vulkan.engineName?.let {
                    pEngineName = it.cstr.ptr
                }
                context.configuration.vulkan.engineVersion?.let {
                    engineVersion = vkMakeVersion(it.major.convert(), it.minor.convert(), it.patch.convert())
                }
            }
        }
    }

    private fun getCreateInfo(memScope: MemScope, appInfo: VkApplicationInfo, context: GameContext): VkInstanceCreateInfo {
        context.instanceExtensions = getExtensions(context)
        context.validationLayers = getValidationLayers(context)

        return with(memScope) {
            alloc {
                sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO
                pApplicationInfo = appInfo.ptr
                enabledExtensionCount = context.instanceExtensions.size.convert()
                ppEnabledExtensionNames = context.instanceExtensions.toList().toCStringArray(memScope)
                enabledLayerCount = context.validationLayers.size.convert()
                ppEnabledLayerNames = context.validationLayers.toList().toCStringArray(memScope)
            }
        }
    }

    private fun getExtensions(context: GameContext): Set<String> {
        val requiredExtensions = context.configuration.vulkan.requiredInstanceExtensions + context.window.getRequiredInstanceExtensions()

        val availableExtensions = getAvailableVulkanInstanceExtensions()
        if (!availableExtensions.containsAll(requiredExtensions)) {
            throw RuntimeException("Some required extensions are not available")
        }

        return requiredExtensions + context.configuration.vulkan.optionalInstanceExtensions.filter {
            availableExtensions.contains(it)
        }
    }

    private fun getValidationLayers(context: GameContext): Set<String> {
        if (!Platform.isDebugBinary) {
            return setOf()
        }

        val availableValidationLayers = getAvailableVulkanValidationLayers()
        if (!availableValidationLayers.containsAll(context.configuration.vulkan.validationLayers)) {
            throw RuntimeException("Some requested validation layers are not available")
        }

        return context.configuration.vulkan.validationLayers
    }

}