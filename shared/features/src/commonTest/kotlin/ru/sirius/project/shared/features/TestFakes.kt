package ru.sirius.project.shared.features

import ru.sirius.project.shared.domain.AuthRepository
import ru.sirius.project.shared.domain.Order
import ru.sirius.project.shared.domain.OrderDraft
import ru.sirius.project.shared.domain.OrderRepository

/** Fake вместо mock: проще читать и не ломается при рефакторинге. */
class FakeAuth(
    signedIn: Boolean = false,
) : AuthRepository {
    override var isSignedIn: Boolean = signedIn
        private set

    override suspend fun signIn() {
        isSignedIn = true
    }

    override suspend fun signOut() {
        isSignedIn = false
    }
}

class FakeOrders(
    private val orders: List<Order> = listOf(Order("1", "NEW", "USD", "10.00", "2026-11-01T00:00:00Z")),
    /** Сколько первых вызовов create() должны завершиться ошибкой. */
    private var failCreateTimes: Int = 0,
) : OrderRepository {
    val usedKeys = mutableListOf<String>()
    val drafts = mutableListOf<OrderDraft>()

    override suspend fun refresh(): List<Order> = orders

    override suspend fun get(id: String): Order? = orders.firstOrNull { it.id == id }

    override suspend fun create(
        draft: OrderDraft,
        idempotencyKey: String,
    ): Order {
        usedKeys += idempotencyKey
        drafts += draft
        if (failCreateTimes > 0) {
            failCreateTimes--
            error("network down")
        }
        return Order("2", "NEW", draft.currency, "3.00", "2026-11-06T00:00:00Z", draft.items)
    }
}
