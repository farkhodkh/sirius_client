package ru.sirius.project.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import ru.sirius.project.shared.domain.AccessTokenProvider
import ru.sirius.project.shared.domain.AppException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Тонкая обёртка над HTTP API order-service. Единственная ответственность: запрос, токен, разбор ошибок.
 * Все сбои превращаются в [AppException], чтобы Ktor не протекал выше слоя data.
 */
internal class OrderApi(
    private val client: HttpClient,
    private val tokens: AccessTokenProvider,
) {
    suspend fun list(
        page: Int,
        size: Int,
    ): PageDto<OrderSummaryDto> =
        execute(HttpMethod.Get, "/api/v1/orders") {
            parameter("page", page)
            parameter("size", size)
        }.parse()

    suspend fun get(id: String): OrderDto = execute(HttpMethod.Get, "/api/v1/orders/$id").parse()

    suspend fun create(
        request: CreateOrderRequestDto,
        idempotencyKey: String,
    ): OrderDto =
        execute(HttpMethod.Post, "/api/v1/orders") {
            header("Idempotency-Key", idempotencyKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.parse()

    private suspend fun execute(
        method: HttpMethod,
        path: String,
        configure: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse {
        // Нет токена: не тратим запрос, сразу сообщаем, что нужен вход.
        val token = tokens.current() ?: throw AppException.Unauthorized()

        val response = send(method, path, token, configure)

        if (!response.status.isSuccess()) throw response.toAppException()
        return response
    }

    /** Отправка запроса: любой сбой транспорта становится [AppException.Network], отмена корутины пробрасывается. */
    private suspend fun send(
        method: HttpMethod,
        path: String,
        token: String,
        configure: HttpRequestBuilder.() -> Unit,
    ): HttpResponse =
        try {
            client.request(path) {
                this.method = method
                bearerAuth(token)
                configure()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AppException.Network(e)
        }

    private suspend inline fun <reified T> HttpResponse.parse(): T =
        try {
            body<T>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AppException.Server(status.value, "Неверный формат ответа сервера", e)
        }

    private suspend fun HttpResponse.toAppException(): AppException {
        val detail = problemDetail()
        return when (status) {
            HttpStatusCode.Unauthorized -> {
                AppException.Unauthorized()
            }

            HttpStatusCode.Forbidden -> {
                AppException.Forbidden()
            }

            HttpStatusCode.NotFound -> {
                AppException.NotFound(detail ?: "Не найдено")
            }

            HttpStatusCode.BadRequest, HttpStatusCode.UnprocessableEntity -> {
                AppException.Validation(detail ?: "Некорректные данные")
            }

            else -> {
                AppException.Server(status.value)
            }
        }
    }

    /**
     * Текст ошибки из problem+json. Читаем как текст и разбираем сами: тип `application/problem+json`
     * не совпадает с `application/json`, на который настроен ContentNegotiation.
     */
    private suspend fun HttpResponse.problemDetail(): String? =
        try {
            val problem = apiJson.decodeFromString(ProblemDto.serializer(), bodyAsText())
            problem.detail ?: problem.title
        } catch (e: CancellationException) {
            throw e
        } catch (ignored: Exception) {
            // Тело ошибки не удалось разобрать: сообщение возьмём по статусу, исходная причина не нужна.
            null
        }
}
