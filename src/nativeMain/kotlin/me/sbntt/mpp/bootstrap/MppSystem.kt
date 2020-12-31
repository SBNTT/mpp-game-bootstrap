package me.sbntt.mpp.bootstrap

import kotlin.system.exitProcess as ktExitProcess

actual object MppSystem {

    actual fun exitProcess(status: Int) {
        ktExitProcess(status)
    }

}