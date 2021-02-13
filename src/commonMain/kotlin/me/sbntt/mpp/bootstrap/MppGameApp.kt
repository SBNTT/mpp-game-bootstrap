package me.sbntt.mpp.bootstrap

class MppGameApp(private val window: MppGameWindow) {

    internal fun run(): Int {
        try {
            window.init()
            mainLoop()
            window.terminate()
        } catch (e: Exception) {
            println(e.message)
            return 1
        }

        return 0
    }

    private fun mainLoop() {
        while (!window.shouldClose()) {
            window.update()

        }
    }

}