package me.sbntt.mppgame.bootstrap

sealed class GameWindowConfiguration(val name: String, val width: Int, val height: Int) {

    class FullScreen(name: String, width: Int, height: Int) : GameWindowConfiguration(name, width, height)

    class Resizable(name: String, width: Int, height: Int) : GameWindowConfiguration(name, width, height)

    class Fixed(name: String, width: Int, height: Int) : GameWindowConfiguration(name, width, height)

}