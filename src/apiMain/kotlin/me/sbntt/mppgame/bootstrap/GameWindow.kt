package me.sbntt.mppgame.bootstrap

expect class GameWindow(configuration: GameWindowConfiguration) {

    internal val configuration: GameWindowConfiguration

    internal fun init()
    internal fun update()
    internal fun shouldClose(): Boolean
    internal fun getRequiredInstanceExtensions(): List<String>
    internal fun terminate()

}