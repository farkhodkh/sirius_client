package ru.sirius.project.shared.features.order.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.sirius.project.shared.features.order.OrderDetailsComponent
import ru.sirius.project.shared.features.order.OrderDetailsState

@Composable
fun OrderDetailsScreen(component: OrderDetailsComponent) {
    val state by component.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedButton(onClick = component::onBackClick) { Text("Назад") }
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            OrderDetailsState.Loading -> CircularProgressIndicator()
            OrderDetailsState.NotFound -> Text("Заказ не найден")
            is OrderDetailsState.Error -> {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = component::load) { Text("Повторить") }
            }
            is OrderDetailsState.Content -> {
                Text("Заказ ${s.order.id}", style = MaterialTheme.typography.headlineSmall)
                Text("Статус: ${s.order.status}")
                Text("Сумма: ${s.order.totalAmount} ${s.order.currency}")
                Text("Создан: ${s.order.createdAt}")
            }
        }
    }
}
