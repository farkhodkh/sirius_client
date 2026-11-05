package ru.sirius.project.shared.features.order

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sirius.project.shared.domain.OrderDraft
import ru.sirius.project.shared.domain.OrderItem
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

    fun onCurrencyChange(value: String) = edit { copy(currency = value) }
    fun onSkuChange(value: String) = edit { copy(sku = value) }
    fun onNameChange(value: String) = edit { copy(name = value) }
    fun onQuantityChange(value: String) = edit { copy(quantity = value) }
    fun onUnitPriceChange(value: String) = edit { copy(unitPrice = value) }

    fun onSubmitClick() {
        val current = _state.value
        if (current.isSubmitting) return   // защита от двойного нажатия

        val problem = current.validationError()
        if (problem != null) {
            _state.update { it.copy(error = problem) }
            return
        }

        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            try {
                repo.create(current.toDraft(), idempotencyKey)
                idempotencyKey = newIdempotencyKey()
                _state.update { it.copy(isSubmitting = false) }
                onFinished()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Ключ НЕ меняем: пользователь повторит то же намерение.
                _state.update { it.copy(isSubmitting = false, error = e.message ?: "Ошибка") }
            }
        }
    }

    fun onBackClick() = onFinished()

    private fun edit(change: OrderCreateState.() -> OrderCreateState) =
        _state.update { it.change().copy(error = null) }
}

data class OrderCreateState(
    val currency: String = "USD",
    val sku: String = "",
    val name: String = "",
    val quantity: String = "1",
    val unitPrice: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

// Те же ограничения, что в CreateOrderRequest на backend: цену до 2 знаков, количество 1..1000 и т.д.
private val PRICE = Regex("^\\d{1,10}(\\.\\d{1,2})?$")

private fun OrderCreateState.validationError(): String? = when {
    currency.trim().length != 3 -> "Валюта: 3 символа, например USD"
    sku.isBlank() -> "Укажите артикул"
    sku.length > 64 -> "Артикул: не больше 64 символов"
    name.isBlank() -> "Укажите название"
    name.length > 255 -> "Название: не больше 255 символов"
    quantity.toIntOrNull()?.let { it in 1..1000 } != true -> "Количество: от 1 до 1000"
    !PRICE.matches(unitPrice) -> "Цена: число не больше чем с двумя знаками после точки, например 12.50"
    else -> null
}

private fun OrderCreateState.toDraft() = OrderDraft(
    currency = currency.trim().uppercase(),
    items = listOf(OrderItem(sku.trim(), name.trim(), quantity.toInt(), unitPrice)),
)

@OptIn(ExperimentalUuidApi::class)
private fun newIdempotencyKey(): String = Uuid.random().toString()
