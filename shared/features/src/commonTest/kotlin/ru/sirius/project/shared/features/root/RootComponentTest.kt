package ru.sirius.project.shared.features.root

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import ru.sirius.project.shared.features.FakeAuth
import ru.sirius.project.shared.features.FakeOrders
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RootComponentTest {
    // Компоненты запускают корутины на Dispatchers.Main (Essenty), в тесте на JVM его нет: подменяем.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun createRoot(signedIn: Boolean): RootComponent {
        val lifecycle = LifecycleRegistry()
        val root =
            RootComponent(
                componentContext = DefaultComponentContext(lifecycle),
                deps = Dependencies(auth = FakeAuth(signedIn), orders = FakeOrders()),
            )
        lifecycle.resume()
        return root
    }

    @Test
    fun withoutSession_startsOnLogin() {
        val root = createRoot(signedIn = false)

        assertIs<RootComponent.Child.Login>(root.stack.value.active.instance)
    }

    @Test
    fun withSession_startsOnOrderList() {
        val root = createRoot(signedIn = true)

        assertIs<RootComponent.Child.OrderList>(root.stack.value.active.instance)
    }

    @Test
    fun afterSignIn_stackIsOnlyOrderList_soBackDoesNotReturnToLogin() {
        val root = createRoot(signedIn = false)

        assertIs<RootComponent.Child.Login>(root.stack.value.active.instance).component.onSignInClick()

        assertIs<RootComponent.Child.OrderList>(root.stack.value.active.instance)
        assertTrue(
            root.stack.value.backStack
                .isEmpty(),
            "после входа Login не должен оставаться в стеке",
        )
    }

    @Test
    fun openOrderAndBack_returnsToList() {
        val root = createRoot(signedIn = true)

        assertIs<RootComponent.Child.OrderList>(root.stack.value.active.instance).component.onOrderClick("1")
        val details = assertIs<RootComponent.Child.OrderDetails>(root.stack.value.active.instance)
        assertEquals(2, root.stack.value.items.size)

        details.component.onBackClick()

        assertIs<RootComponent.Child.OrderList>(root.stack.value.active.instance)
    }

    @Test
    fun createOrder_finishesAndReturnsToList() {
        val root = createRoot(signedIn = true)

        assertIs<RootComponent.Child.OrderList>(root.stack.value.active.instance).component.onCreateClick()
        val create = assertIs<RootComponent.Child.OrderCreate>(root.stack.value.active.instance).component
        create.onSkuChange("A1")
        create.onNameChange("Pen")
        create.onUnitPriceChange("15.00")
        create.onSubmitClick()

        assertIs<RootComponent.Child.OrderList>(root.stack.value.active.instance)
    }
}
