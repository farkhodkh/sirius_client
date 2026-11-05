package ru.sirius.project.shared.features.order

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderListComponent(
    componentContext: ComponentContext,
    private val repo: OrderRepository,
    private val onCreate: () -> Unit,
    private val onOpen: (String) -> Unit,
) : ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val _state = MutableStateFlow<OrderListState>(OrderListState.Loading)
    val state: StateFlow<OrderListState> = _state

    init { refresh() }

    fun refresh() = scope.launch {
        _state.value = OrderListState.Loading
        _state.value = runCatching { repo.refresh() }
            .fold({ OrderListState.Content(it) }, { OrderListState.Error(it.message ?: "Ошибка") })
    }
}

sealed interface OrderListState {
    data object Loading : OrderListState
    data class Content(val orders: List<Order>) : OrderListState
    data class Error(val message: String) : OrderListState
}