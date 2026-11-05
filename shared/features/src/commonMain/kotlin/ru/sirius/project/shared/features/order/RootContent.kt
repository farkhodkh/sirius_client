package ru.sirius.project.shared.features.order

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import ru.sirius.project.shared.features.order.screen.LoginScreen
import ru.sirius.project.shared.features.order.screen.OrderCreateScreen
import ru.sirius.project.shared.features.order.screen.OrderDetailsScreen
import ru.sirius.project.shared.features.order.screen.OrderListScreen
import ru.sirius.project.shared.features.root.RootComponent

@Composable
fun RootContent(root: RootComponent) {
    Children(stack = root.stack, animation = stackAnimation(fade())) { child ->
        when (val c = child.instance) {
            is RootComponent.Child.Login -> LoginScreen(c.component)
            is RootComponent.Child.OrderList -> OrderListScreen(c.component)
            is RootComponent.Child.OrderCreate -> OrderCreateScreen(c.component)
            is RootComponent.Child.OrderDetails -> OrderDetailsScreen(c.component)
        }
    }
}