package ru.sirius.project.shared.features.order.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.sirius.project.shared.features.order.OrderCreateComponent

@Composable
fun OrderCreateScreen(component: OrderCreateComponent) {
    val state by component.state.collectAsState()
    val enabled = !state.isSubmitting

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Text("Новый заказ", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Field("Артикул (sku)", state.sku, component::onSkuChange, enabled)
        Field("Название", state.name, component::onNameChange, enabled)
        Field("Количество", state.quantity, component::onQuantityChange, enabled)
        Field("Цена за единицу", state.unitPrice, component::onUnitPriceChange, enabled)
        Field("Валюта", state.currency, component::onCurrencyChange, enabled)

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Row {
            OutlinedButton(onClick = component::onBackClick, enabled = enabled) { Text("Отмена") }
            Spacer(Modifier.width(12.dp))
            Button(onClick = component::onSubmitClick, enabled = enabled) {
                Text(if (state.isSubmitting) "Отправка…" else "Создать")
            }
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    )
}
