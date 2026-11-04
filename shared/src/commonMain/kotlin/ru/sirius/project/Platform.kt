package ru.sirius.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform