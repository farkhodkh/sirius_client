package ru.sirius.project.shared.domain

/**
 * Ошибки, которые видит остальное приложение. Детали Ktor и HTTP остаются в слое data.
 * Экраны решают, что показать: 401 ведёт на вход, Network даёт «повторить» и т.д.
 */
sealed class AppException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /** Нет токена, он просрочен или отклонён (HTTP 401). */
    class Unauthorized(
        message: String = "Требуется вход",
    ) : AppException(message)

    /** Токен есть, но прав недостаточно (HTTP 403, не хватает scope). */
    class Forbidden(
        message: String = "Недостаточно прав",
    ) : AppException(message)

    class NotFound(
        message: String = "Не найдено",
    ) : AppException(message)

    /** Backend отклонил данные (HTTP 400/422). [message] берётся из problem+json. */
    class Validation(
        message: String,
    ) : AppException(message)

    /** Нет связи, таймаут, обрыв соединения. */
    class Network(
        cause: Throwable? = null,
    ) : AppException("Нет связи с сервером", cause)

    /** Ошибка сервера (5xx) или непонятный ответ. */
    class Server(
        val code: Int? = null,
        message: String = "Ошибка сервера",
        cause: Throwable? = null,
    ) : AppException(message, cause)
}
