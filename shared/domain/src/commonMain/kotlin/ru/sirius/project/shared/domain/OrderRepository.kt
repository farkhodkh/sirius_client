package ru.sirius.project.shared.domain

interface OrderRepository {
    /** Первая страница заказов (в главе 6 станет: сеть -> локальная БД). */
    suspend fun refresh(): List<Order>

    /** Карточка заказа с позициями или null, если заказа нет. */
    suspend fun get(id: String): Order?

    /**
     * Создать заказ. [idempotencyKey] (8-64 символа) генерируется один раз на «намерение» пользователя
     * и повторяется при ретрае: backend вернёт тот же заказ, а не создаст второй.
     *
     * @throws AppException при ошибках сети, авторизации и валидации
     */
    suspend fun create(draft: OrderDraft, idempotencyKey: String): Order
}
