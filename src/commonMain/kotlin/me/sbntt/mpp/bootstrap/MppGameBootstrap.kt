package me.sbntt.mpp.bootstrap

expect object MppGameBootstrap {

    fun run(init: () -> MppGameApp)

}