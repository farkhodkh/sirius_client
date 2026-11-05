package ru.sirius.project.shared.features.order.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Новый заказ", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.totalAmount,
            onValueChange = component::onAmountChange,
            label = { Text("Сумма") },
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.currency,
            onValueChange = component::onCurrencyChange,
            label = { Text("Валюта") },
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Row {
            OutlinedButton(onClick = component::onBackClick, enabled = !state.isSubmitting) { Text("Отмена") }
            Spacer(Modifier.width(12.dp))
            Button(onClick = component::onSubmitClick, enabled = !state.isSubmitting) {
                Text(if (state.isSubmitting) "Отправка…" else "Создать")
            }
        }
    }
}
