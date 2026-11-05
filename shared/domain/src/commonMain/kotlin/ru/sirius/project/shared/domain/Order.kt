package ru.sirius.project.shared.domain

/**
 * Заказ. Сумма хранится строкой: деньги не в Double (арифметику с деньгами делает backend).
 * Поля уточнить по `CreateOrderRequest`/`OrderResponse` в order-service (глава 5).
 */
data class Order(
    val id: String,
    val status: String,
    val currency: String,
    val totalAmount: String,
    val createdAt: String,
)

/** Данные формы создания заказа. Минимальный набор для скелета. */
data class OrderDraft(
    val currency: String,
    val totalAmount: String,
)
