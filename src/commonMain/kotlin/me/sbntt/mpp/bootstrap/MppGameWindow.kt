package me.sbntt.mpp.bootstrap

expect class MppGameWindow(name: String, width: Int, height: Int) {

    val name: String
    val width: Int
    val height: Int

    internal fun init()
    internal fun update()
    internal fun shouldClose(): Boolean
    internal fun terminate()

}