package ru.sirius.project.shared.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import ru.sirius.project.shared.data.remote.ApiConfig
import ru.sirius.project.shared.data.remote.createHttpClient
import ru.sirius.project.shared.domain.AccessTokenProvider
import ru.sirius.project.shared.domain.AppException
import ru.sirius.project.shared.domain.OrderDraft
import ru.sirius.project.shared.domain.OrderItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OrderRepositoryImplTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val problemHeaders = headersOf(HttpHeaders.ContentType, "application/problem+json")

    private val orderJson = """
        {"id":"11111111-1111-1111-1111-111111111111","status":"NEW","totalAmount":3.00,"currency":"USD",
         "createdAt":"2026-11-06T10:00:00Z",
         "items":[{"sku":"A1","name":"Pen","quantity":2,"unitPrice":1.50}]}
    """.trimIndent()

    private val draft = OrderDraft("USD", listOf(OrderItem("A1", "Pen", 2, "1.50")))

    private fun createRepo(token: String? = "token-123", handler: MockRequestHandler): Pair<OrderRepositoryImpl, MockEngine> {
        val engine = MockEngine(handler)
        val client = createHttpClient(ApiConfig("https://api.test"), engine)
        return OrderRepositoryImpl(client, AccessTokenProvider { token }) to engine
    }

    @Test
    fun create_sendsBearerIdempotencyKeyAndNumericDecimal() = runTest {
        val (repo, engine) = createRepo { respond(orderJson, HttpStatusCode.Created, jsonHeaders) }

        val order = repo.create(draft, "key-0001-abcd")

        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/v1/orders", request.url.encodedPath)
        assertEquals("Bearer token-123", request.headers[HttpHeaders.Authorization])
        assertEquals("key-0001-abcd", request.headers["Idempotency-Key"])
        val body = (request.body as TextContent).text
        assertTrue(""""unitPrice":1.50""" in body, "цена должна уходить числом без потери нуля: $body")

        assertEquals("11111111-1111-1111-1111-111111111111", order.id)
        assertEquals("3.00", order.totalAmount)           // точность из ответа сохранена, без Double
        assertEquals("1.50", order.items.single().unitPrice)
    }

    @Test
    fun retry_usesSameIdempotencyKey() = runTest {
        val (repo, engine) = createRepo { respond(orderJson, HttpStatusCode.Created, jsonHeaders) }

        repo.create(draft, "key-0001-abcd")
        repo.create(draft, "key-0001-abcd")

        val keys = engine.requestHistory.map { it.headers["Idempotency-Key"] }
        assertEquals(listOf("key-0001-abcd", "key-0001-abcd"), keys)
    }

    @Test
    fun noToken_throwsUnauthorizedWithoutRequest() = runTest {
        val (repo, engine) = createRepo(token = null) { respond(orderJson, HttpStatusCode.Created, jsonHeaders) }

        assertFailsWith<AppException.Unauthorized> { repo.create(draft, "key-0001-abcd") }

        assertTrue(engine.requestHistory.isEmpty(), "без токена запрос уходить не должен")
    }

    @Test
    fun http401_mapsToUnauthorized() = runTest {
        val (repo, _) = createRepo { respond("", HttpStatusCode.Unauthorized) }

        assertFailsWith<AppException.Unauthorized> { repo.refresh() }
    }

    @Test
    fun http403_mapsToForbidden() = runTest {
        val (repo, _) = createRepo { respond("", HttpStatusCode.Forbidden) }

        assertFailsWith<AppException.Forbidden> { repo.create(draft, "key-0001-abcd") }
    }

    @Test
    fun http400_problemJson_mapsToValidationWithDetail() = runTest {
        val problem = """{"title":"Bad Request","status":400,"detail":"currency size must be between 3 and 3"}"""
        val (repo, _) = createRepo { respond(problem, HttpStatusCode.BadRequest, problemHeaders) }

        val error = assertFailsWith<AppException.Validation> { repo.create(draft, "key-0001-abcd") }

        assertEquals("currency size must be between 3 and 3", error.message)
    }

    @Test
    fun http500_mapsToServerWithCode() = runTest {
        val (repo, _) = createRepo { respond("oops", HttpStatusCode.InternalServerError) }

        val error = assertFailsWith<AppException.Server> { repo.refresh() }

        assertEquals(500, error.code)
    }

    @Test
    fun connectionFailure_mapsToNetwork() = runTest {
        val (repo, _) = createRepo { error("connection reset") }

        assertFailsWith<AppException.Network> { repo.refresh() }
    }

    @Test
    fun get_notFound_returnsNull() = runTest {
        val (repo, _) = createRepo {
            respond("""{"title":"order_not_found","detail":"Order not found"}""", HttpStatusCode.NotFound, problemHeaders)
        }

        assertNull(repo.get("22222222-2222-2222-2222-222222222222"))
    }

    @Test
    fun refresh_parsesPage_summariesHaveNoItems() = runTest {
        val page = """
            {"content":[{"id":"1","status":"NEW","totalAmount":12.5,"currency":"USD","createdAt":"2026-11-01T00:00:00Z"}],
             "page":0,"size":50,"totalElements":1,"totalPages":1}
        """.trimIndent()
        val (repo, engine) = createRepo { respond(page, HttpStatusCode.OK, jsonHeaders) }

        val orders = repo.refresh()

        assertEquals(1, orders.size)
        assertEquals("12.5", orders.single().totalAmount)
        assertTrue(orders.single().items.isEmpty())
        assertEquals("0", engine.requestHistory.single().url.parameters["page"])
    }

    @Test
    fun malformedBody_mapsToServer() = runTest {
        val (repo, _) = createRepo { respond("""{"unexpected":true}""", HttpStatusCode.OK, jsonHeaders) }

        assertFailsWith<AppException.Server> { repo.refresh() }
    }
}
