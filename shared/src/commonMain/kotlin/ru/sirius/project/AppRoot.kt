package ru.sirius.project

import com.arkivanov.decompose.ComponentContext
import ru.sirius.project.shared.data.FakeAuthRepository
import ru.sirius.project.shared.data.FakeOrderRepository
import ru.sirius.project.shared.features.root.Dependencies
import ru.sirius.project.shared.features.root.RootComponent

/**
 * Единственное место, где features и data встречаются: собираем зависимости и корневой компонент.
 * Платформенные приложения (Android, iOS, Web) вызывают только эту функцию.
 * Позже (глава 4, шаг 7) здесь появится Koin, а в главах 5-6 Fake-реализации заменятся настоящими.
 */
fun createRoot(componentContext: ComponentContext): RootComponent =
    RootComponent(
        componentContext = componentContext,
        deps = Dependencies(
            auth = FakeAuthRepository(),
            orders = FakeOrderRepository(),
        ),
    )
