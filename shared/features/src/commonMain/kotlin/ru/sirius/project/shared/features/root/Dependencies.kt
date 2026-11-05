package ru.sirius.project.shared.features.root

import ru.sirius.project.shared.domain.AuthRepository
import ru.sirius.project.shared.domain.OrderRepository

/**
 * Зависимости, из которых RootComponent собирает дочерние компоненты.
 * Создаётся на уровне приложения (модуль :shared), features про реализации ничего не знает.
 */
class Dependencies(
    val auth: AuthRepository,
    val orders: OrderRepository,
)
