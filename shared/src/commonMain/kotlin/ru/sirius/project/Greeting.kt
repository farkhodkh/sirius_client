package ru.sirius.project

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = sayHello(platform.name)
}
