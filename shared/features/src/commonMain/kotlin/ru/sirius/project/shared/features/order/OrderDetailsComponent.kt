package ru.sirius.project.shared.features.order

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.sirius.project.shared.domain.Order
import ru.sirius.project.shared.domain.OrderRepository

class OrderDetailsComponent(
    componentContext: ComponentContext,
    private val repo: OrderRepository,
    private val onOpenOrder: () -> Unit,
    private val onOpen: (String) -> Unit,
) : ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val _state = MutableStateFlow<OrderDetailsState>(OrderDetailsState.Loading)
    val state: StateFlow<OrderDetailsState> = _state

    init { refresh() }

    fun refresh() = scope.launch {
        _state.value = OrderDetailsState.Loading
        _state.value = runCatching { repo.refresh() }
            .fold({ OrderDetailsState.Content(it) }, { OrderDetailsState.Error(it.message ?: "Ошибка") })
    }
}

sealed interface OrderDetailsState {
    data object Loading : OrderListState, OrderDetailsState
    data class Content(val orders: List<Order>) : OrderDetailsState

    data class Error(val message: String) : OrderDetailsState
}