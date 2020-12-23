package me.sbntt.mpp.bootstrap

sealed class Platform
sealed class Desktop : Platform()
sealed class Mobile : Platform()

object Windows : Desktop()
object Macos : Desktop()
object Linux : Desktop()

object Android : Mobile()
object Ios : Mobile()

expect fun currentPlatform(): Platform