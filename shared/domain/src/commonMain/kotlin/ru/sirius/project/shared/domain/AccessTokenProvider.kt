package ru.sirius.project.shared.domain

/**
 * Источник текущего access-токена для запросов к API.
 * Токен живёт 10 минут, refresh-токена у публичного клиента нет, поэтому реализация
 * (глава 5, шаг 4) хранит токен только в памяти и возвращает null, если он отсутствует или просрочен.
 */
fun interface AccessTokenProvider {
    fun current(): String?
}
