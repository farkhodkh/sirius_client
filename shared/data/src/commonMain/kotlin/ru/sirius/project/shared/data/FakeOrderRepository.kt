package ru.sirius.project.shared.data

import ru.sirius.project.shared.domain.Order
import ru.sirius.project.shared.domain.OrderDraft
import ru.sirius.project.shared.domain.OrderItem
import ru.sirius.project.shared.domain.OrderRepository

/**
 * In-memory реализация для превью и офлайн-разработки, когда backend недоступен.
 * Идемпотентность имитирует order-service: тот же ключ -> тот же заказ.
 */
class FakeOrderRepository : OrderRepository {
    private val orders = mutableListOf(
        Order(
            "1", "NEW", "USD", "3.00", "2026-11-01T10:00:00Z",
            items = listOf(OrderItem("A1", "Pen", 2, "1.50")),
        ),
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
                totalAmount = totalOf(draft),
                createdAt = "2026-11-06T00:00:00Z",
                items = draft.items,
            ).also { orders.add(0, it) }
        }

    // Сумма в минорных единицах (Long), без Double.
    private fun totalOf(draft: OrderDraft): String {
        val cents = draft.items.sumOf { centsOf(it.unitPrice) * it.quantity }
        return "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
    }

    private fun centsOf(price: String): Long {
        val parts = price.split('.')
        val whole = parts[0].toLongOrNull() ?: 0L
        val fraction = (parts.getOrNull(1) ?: "").padEnd(2, '0').take(2).toLongOrNull() ?: 0L
        return whole * 100 + fraction
    }
}
