package ru.sirius.project.shared.data

import ru.sirius.project.shared.domain.Order
import ru.sirius.project.shared.domain.OrderDraft
import ru.sirius.project.shared.domain.OrderRepository

/**
 * Временная in-memory реализация, чтобы скелет приложения работал без backend.
 * Идемпотентность имитирует поведение order-service: тот же ключ -> тот же заказ.
 * Заменить реализацией на Ktor + SQLDelight в главах 5-6.
 */
class FakeOrderRepository : OrderRepository {
    private val orders = mutableListOf(
        Order("1", "NEW", "USD", "12.50", "2026-11-01T10:00:00Z"),
        Order("2", "PAID", "USD", "99.00", "2026-11-02T12:30:00Z"),
    )
    private val createdByKey = mutableMapOf<String, Order>()

    override suspend fun refresh(): List<Order> = orders.toList()

    override suspend fun get(id: String): Order? = orders.firstOrNull { it.id == id }

    override suspend fun create(draft: OrderDraft, idempotencyKey: String): Order =
        createdByKey.getOrPut(idempotencyKey) {
            Order(
                id = (orders.size + 1).toString(),
                status = "NEW",
                currency = draft.currency,
                totalAmount = draft.totalAmount,
                createdAt = "2026-11-06T00:00:00Z",
            ).also { orders.add(0, it) }
        }
}
