package ru.sirius.project.shared.features.order.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.sirius.project.shared.features.order.OrderListComponent
import ru.sirius.project.shared.features.order.OrderListState

@Composable
fun OrderListScreen(component: OrderListComponent) {
    val state by component.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Заказы", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = component::onCreateClick) { Text("Создать") }
        }
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            OrderListState.Loading -> CircularProgressIndicator()
            OrderListState.Empty -> Text("Заказов пока нет")
            is OrderListState.Error -> {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = component::refresh) { Text("Повторить") }
            }
            is OrderListState.Content -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(s.orders, key = { it.id }) { order ->
                    Card(onClick = { component.onOrderClick(order.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Заказ ${order.id.take(8)} · ${order.status}")   // id на backend - UUID, показываем начало
                            Text("${order.totalAmount} ${order.currency}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
