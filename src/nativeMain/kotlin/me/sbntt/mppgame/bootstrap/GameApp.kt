package me.sbntt.mppgame.bootstrap

import kotlinx.cinterop.MemScope

internal class GameApp(memScope: MemScope, configuration: GameConfiguration) : VulkanApp(memScope, configuration) {

    internal fun run(): Int {
        try {
            init()
            mainLoop()
            terminate()
        } catch (e: Exception) {
            println(e.message)
            return 1
        }

        return 0
    }

    private fun mainLoop() {
        while (!context.window.shouldClose()) {
            context.window.update()

        }
    }

}