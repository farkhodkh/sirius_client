package ru.sirius.project.shared.features.root

import kotlinx.serialization.Serializable

@Serializable
sealed interface Config {
    @Serializable data object Login : Config

    @Serializable data object OrderList : Config

    @Serializable data object OrderCreate : Config

    @Serializable data class OrderDetails(
        val orderId: String,
    ) : Config
}
