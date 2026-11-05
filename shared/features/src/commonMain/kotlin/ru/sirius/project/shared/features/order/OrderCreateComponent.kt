package ru.sirius.project.shared.features.order

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sirius.project.shared.domain.OrderDraft
import ru.sirius.project.shared.domain.OrderRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class OrderCreateComponent(
    componentContext: ComponentContext,
    private val repo: OrderRepository,
    private val onFinished: () -> Unit,
) : ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val _state = MutableStateFlow(OrderCreateState())
    val state: StateFlow<OrderCreateState> = _state.asStateFlow()

    // Один ключ на «намерение» пользователя: при ретрае после ошибки уходит ТОТ ЖЕ ключ,
    // поэтому backend не создаст второй заказ. После успеха ключ обновляется.
    private var idempotencyKey = newIdempotencyKey()

    fun onCurrencyChange(value: String) = _state.update { it.copy(currency = value, error = null) }

    fun onAmountChange(value: String) = _state.update { it.copy(totalAmount = value, error = null) }

    fun onSubmitClick() {
        val current = _state.value
        if (current.isSubmitting) return   // защита от двойного нажатия
        if (current.totalAmount.isBlank()) {
            _state.update { it.copy(error = "Укажите сумму") }
            return
        }
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            try {
                repo.create(OrderDraft(current.currency, current.totalAmount), idempotencyKey)
                idempotencyKey = newIdempotencyKey()
                _state.update { it.copy(isSubmitting = false) }
                onFinished()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isSubmitting = false, error = e.message ?: "Ошибка") }
            }
        }
    }

    fun onBackClick() = onFinished()
}

data class OrderCreateState(
    val currency: String = "USD",
    val totalAmount: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

@OptIn(ExperimentalUuidApi::class)
private fun newIdempotencyKey(): String = Uuid.random().toString()
