package ru.sirius.project.shared.features.order

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import ru.sirius.project.shared.features.FakeOrders
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class OrderCreateComponentTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun component(repo: FakeOrders, onFinished: () -> Unit): OrderCreateComponent {
        val lifecycle = LifecycleRegistry()
        return OrderCreateComponent(DefaultComponentContext(lifecycle), repo, onFinished)
            .also { lifecycle.resume() }
    }

    @Test
    fun retryAfterFailure_usesSameIdempotencyKey() {
        val repo = FakeOrders(failCreateTimes = 1)
        var finished = 0
        val component = component(repo) { finished++ }
        component.onAmountChange("10.00")

        component.onSubmitClick()                       // первая попытка падает
        assertNotNull(component.state.value.error)
        component.onSubmitClick()                       // повтор пользователя

        assertEquals(2, repo.usedKeys.size)
        assertEquals(repo.usedKeys[0], repo.usedKeys[1], "при ретрае ключ обязан совпадать")
        assertEquals(1, finished)
    }

    @Test
    fun nextOrderAfterSuccess_getsNewKey() {
        val repo = FakeOrders()
        val component = component(repo) {}
        component.onAmountChange("10.00")

        component.onSubmitClick()
        component.onSubmitClick()

        assertEquals(2, repo.usedKeys.size)
        assertNotEquals(repo.usedKeys[0], repo.usedKeys[1], "после успеха ключ должен смениться")
    }

    @Test
    fun emptyAmount_isRejectedWithoutRequest() {
        val repo = FakeOrders()
        val component = component(repo) {}

        component.onSubmitClick()

        assertEquals(0, repo.usedKeys.size)
        assertNotNull(component.state.value.error)
    }
}
