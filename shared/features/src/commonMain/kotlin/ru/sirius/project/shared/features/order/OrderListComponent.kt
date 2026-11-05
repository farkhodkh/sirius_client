package ru.sirius.project.shared.features.order

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.sirius.project.shared.domain.Order
import ru.sirius.project.shared.domain.OrderRepository
import kotlin.coroutines.cancellation.CancellationException

class OrderListComponent(
    componentContext: ComponentContext,
    private val repo: OrderRepository,
    private val onCreate: () -> Unit,
    private val onOpen: (String) -> Unit,
) : ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val _state = MutableStateFlow<OrderListState>(OrderListState.Loading)
    val state: StateFlow<OrderListState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            _state.value = OrderListState.Loading
            _state.value = try {
                val orders = repo.refresh()
                if (orders.isEmpty()) OrderListState.Empty else OrderListState.Content(orders)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                OrderListState.Error(e.message ?: "Ошибка")
            }
        }
    }

    fun onCreateClick() = onCreate()

    fun onOrderClick(id: String) = onOpen(id)
}

sealed interface OrderListState {
    data object Loading : OrderListState
    data object Empty : OrderListState
    data class Content(val orders: List<Order>) : OrderListState
    data class Error(val message: String) : OrderListState
}
