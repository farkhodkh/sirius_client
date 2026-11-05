package ru.sirius.project.shared.data

import io.ktor.client.HttpClient
import ru.sirius.project.shared.data.remote.OrderApi
import ru.sirius.project.shared.data.remote.toDomain
import ru.sirius.project.shared.data.remote.toDto
import ru.sirius.project.shared.domain.AccessTokenProvider
import ru.sirius.project.shared.domain.AppException
import ru.sirius.project.shared.domain.Order
import ru.sirius.project.shared.domain.OrderDraft
import ru.sirius.project.shared.domain.OrderRepository

/**
 * Репозиторий поверх HTTP API. В главе 6 между сетью и UI появится локальная БД (offline-first),
 * контракт [OrderRepository] при этом не изменится.
 */
class OrderRepositoryImpl(
    client: HttpClient,
    tokens: AccessTokenProvider,
) : OrderRepository {

    private val api = OrderApi(client, tokens)

    override suspend fun refresh(): List<Order> =
        api.list(page = 0, size = PAGE_SIZE).content.map { it.toDomain() }

    override suspend fun get(id: String): Order? =
        try {
            api.get(id).toDomain()
        } catch (e: AppException.NotFound) {
            null
        }

    override suspend fun create(draft: OrderDraft, idempotencyKey: String): Order =
        api.create(draft.toDto(), idempotencyKey).toDomain()

    private companion object {
        const val PAGE_SIZE = 50
    }
}
