package ru.sirius.project.shared.domain

interface OrderRepository {
    /** Обновить список заказов (в главе 6 станет: сеть -> локальная БД). */
    suspend fun refresh(): List<Order>

    suspend fun get(id: String): Order?

    /**
     * Создать заказ. [idempotencyKey] генерируется один раз на «намерение» пользователя
     * и повторяется при ретрае: backend вернёт тот же заказ, а не создаст второй.
     */
    suspend fun create(draft: OrderDraft, idempotencyKey: String): Order
}
