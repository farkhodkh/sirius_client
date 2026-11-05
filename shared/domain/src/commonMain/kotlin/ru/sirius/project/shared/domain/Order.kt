package ru.sirius.project.shared.domain

/**
 * Позиция заказа. Цена строкой: деньги не в Double (арифметику с деньгами делает backend).
 * Ограничения backend: sku до 64 символов, name до 255, quantity 1..1000, цена до 2 знаков после точки.
 */
data class OrderItem(
    val sku: String,
    val name: String,
    val quantity: Int,
    val unitPrice: String,
)

/**
 * Заказ. В списке backend отдаёт краткое представление без позиций, тогда [items] пуст;
 * полный список позиций есть только в карточке заказа.
 */
data class Order(
    val id: String,
    val status: String,
    val currency: String,
    val totalAmount: String,
    val createdAt: String,
    val items: List<OrderItem> = emptyList(),
)

/** Данные формы создания заказа (соответствует CreateOrderRequest в order-service). */
data class OrderDraft(
    val currency: String,
    val items: List<OrderItem>,
)
