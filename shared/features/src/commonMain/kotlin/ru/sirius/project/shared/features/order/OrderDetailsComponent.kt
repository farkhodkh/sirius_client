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

class OrderDetailsComponent(
    componentContext: ComponentContext,
    private val orderId: String,
    private val repo: OrderRepository,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {
    private val scope = coroutineScope()
    private val _state = MutableStateFlow<OrderDetailsState>(OrderDetailsState.Loading)
    val state: StateFlow<OrderDetailsState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        scope.launch {
            _state.value = OrderDetailsState.Loading
            _state.value =
                try {
                    repo.get(orderId)?.let { OrderDetailsState.Content(it) } ?: OrderDetailsState.NotFound
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    OrderDetailsState.Error(e.message ?: "Ошибка")
                }
        }
    }

    fun onBackClick() = onBack()
}

sealed interface OrderDetailsState {
    data object Loading : OrderDetailsState

    data object NotFound : OrderDetailsState

    data class Content(
        val order: Order,
    ) : OrderDetailsState

    data class Error(
        val message: String,
    ) : OrderDetailsState
}
