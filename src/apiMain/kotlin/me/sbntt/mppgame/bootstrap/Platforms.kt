package me.sbntt.mppgame.bootstrap

sealed class Platforms {

    sealed class Desktop : Platforms() {
        object Windows : Desktop()
        object Macos : Desktop()
        object Linux : Desktop()
    }

    sealed class Mobile : Platforms() {
        object Android : Mobile()
        object Ios : Mobile()
    }

    override fun toString() = this::class.simpleName!!

}