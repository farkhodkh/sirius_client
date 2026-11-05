package ru.sirius.project.shared.data.remote

import ru.sirius.project.shared.domain.Order
import ru.sirius.project.shared.domain.OrderDraft
import ru.sirius.project.shared.domain.OrderItem

internal fun OrderItem.toDto() = ItemDto(sku = sku, name = name, quantity = quantity, unitPrice = unitPrice)

internal fun ItemDto.toDomain() = OrderItem(sku = sku, name = name, quantity = quantity, unitPrice = unitPrice)

internal fun OrderDraft.toDto() = CreateOrderRequestDto(currency = currency, items = items.map { it.toDto() })

internal fun OrderDto.toDomain() =
    Order(
        id = id,
        status = status,
        currency = currency,
        totalAmount = totalAmount,
        createdAt = createdAt,
        items = items.map { it.toDomain() },
    )

internal fun OrderSummaryDto.toDomain() =
    Order(
        id = id,
        status = status,
        currency = currency,
        totalAmount = totalAmount,
        createdAt = createdAt,
    )
