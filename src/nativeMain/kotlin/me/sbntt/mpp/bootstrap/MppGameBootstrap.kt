package me.sbntt.mpp.bootstrap

import kotlinx.cinterop.memScoped

actual object MppGameBootstrap {

    actual fun run(init: () -> MppGameApp) {
        memScoped {
            init().run()
        }
    }

}