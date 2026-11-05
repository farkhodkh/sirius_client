package ru.sirius.project.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Адреса и параметры API. Значения по окружениям задаёт приложение (:shared), а не этот модуль. */
data class ApiConfig(
    /** Например https://api.sirius.test (без завершающего слэша). */
    val apiBaseUrl: String,
)

internal val apiJson = Json {
    ignoreUnknownKeys = true   // backend может добавить поля, клиент не должен падать
}

/**
 * Создаёт HTTP-клиент. Если [engine] не указан, Ktor берёт движок платформы из зависимостей
 * (OkHttp на Android, Darwin на iOS, JS на Web). В тестах передаём MockEngine.
 *
 * Плагина Auth здесь нет намеренно: refresh-токена у публичного клиента нет, а токен подставляет
 * [OrderApi] на каждый запрос. Логирование запросов не подключено, чтобы токен не попал в лог.
 */
fun createHttpClient(config: ApiConfig, engine: HttpClientEngine? = null): HttpClient =
    if (engine == null) {
        HttpClient { configure(config) }
    } else {
        HttpClient(engine) { configure(config) }
    }

private fun HttpClientConfig<*>.configure(config: ApiConfig) {
    install(ContentNegotiation) { json(apiJson) }
    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 15_000
    }
    defaultRequest { url(config.apiBaseUrl) }
}
