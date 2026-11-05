package ru.sirius.project.shared.features.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import ru.sirius.project.shared.features.login.LoginComponent
import ru.sirius.project.shared.features.order.OrderCreateComponent
import ru.sirius.project.shared.features.order.OrderDetailsComponent
import ru.sirius.project.shared.features.order.OrderListComponent

class RootComponent(
    componentContext: ComponentContext,
    private val deps: Dependencies,          // фабрики дочерних компонентов (через Koin или вручную)
    private val authState: AuthState,        // из domain: есть ли валидная сессия
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val stack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = if (authState.isSignedIn) Config.OrderList else Config.Login,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, ctx: ComponentContext): Child = when (config) {
        Config.Login -> Child.Login(
            LoginComponent(
                ctx,
                deps.signIn
            ) { navigation.replaceAll(Config.OrderList) })
        Config.OrderList -> Child.OrderList(OrderListComponent(ctx, deps.orders,
            onCreate = { navigation.pushNew(Config.OrderCreate) },
            onOpen = { id -> navigation.pushNew(Config.OrderDetails(id)) }))
        Config.OrderCreate -> Child.OrderCreate(
            OrderCreateComponent(
                ctx,
                deps.createOrder
            ) { navigation.pop() })
        is Config.OrderDetails -> Child.OrderDetails(OrderDetailsComponent(ctx, config.orderId, deps.orders) { navigation.pop() })
    }

    sealed interface Child {
        class Login(val component: LoginComponent) : Child
        class OrderList(val component: OrderListComponent) : Child
        class OrderCreate(val component: OrderCreateComponent) : Child
        class OrderDetails(val component: OrderDetailsComponent) : Child
    }
}