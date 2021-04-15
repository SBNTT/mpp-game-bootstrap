package me.sbntt.mppgame.bootstrap

import kotlinx.cinterop.memScoped

actual fun runGame(configuration: GameConfiguration) {
    memScoped {
        GameApp(this, configuration).run()
    }
}