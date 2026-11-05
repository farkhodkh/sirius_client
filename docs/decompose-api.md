# Decompose, Essenty и связанные API: справка для агента

Назначение: агент не должен писать API по памяти. Здесь два раздела:
1. **Проверено сборкой**: сигнатуры, которые уже компилируются в этом проекте (Decompose `3.4.0-alpha03`, Essenty `2.5.0`, Kotlin `2.4.21`, Compose Multiplatform `1.12.2`). Можно использовать как образец.
2. **Вставить из документации**: выдержки, которые нужно добавить вручную. Без них агент обязан помечать код ⚠️.

Источник версий: `gradle/libs.versions.toml`. При смене версии заново проверьте раздел 1.

## 1. Проверено сборкой (Android, iOS-симулятор, Web: сборка проходила)

Ссылки на файлы проекта даны, чтобы смотреть работающий пример.

### Навигация (`shared/features/.../root/RootComponent.kt`)

```kotlin
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value

class RootComponent(componentContext: ComponentContext, ...) : ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    val stack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),          // Config: @Serializable sealed interface
        initialConfiguration = Config.Login,
        handleBackButton = true,
        childFactory = ::createChild,              // (Config, ComponentContext) -> Child
    )
}
// Переходы: navigation.pushNew(cfg), navigation.pop(), navigation.replaceAll(cfg)
// Чтение в тестах: root.stack.value.active.instance, .backStack, .items
```

### Compose-рендер стека (`root/RootContent.kt`)

```kotlin
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

Children(stack = root.stack, animation = stackAnimation(fade())) { child -> /* child.instance */ }
```

### Корутины компонента (Essenty)

```kotlin
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope   // НЕ kotlinx.coroutines.coroutineScope
private val scope = coroutineScope()    // привязан к lifecycle компонента, Dispatchers.Main
```

### Точки входа

```kotlin
// Android (androidApp/.../MainActivity.kt)
import com.arkivanov.decompose.defaultComponentContext
val root = createRoot(defaultComponentContext())

// iOS (shared/iosMain/.../MainViewController.kt) и Web (webApp/.../main.kt): lifecycle вручную
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
val lifecycle = LifecycleRegistry()
val root = createRoot(DefaultComponentContext(lifecycle))
lifecycle.resume()
```

### Тесты компонентов на JVM

```kotlin
// Essenty-scope использует Dispatchers.Main, на JVM его нет: подменяем (kotlinx-coroutines-test)
@BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
@AfterTest fun tearDown() = Dispatchers.resetMain()
```

## 2. Проверено только чтением руководства, сборкой НЕ проверено (⚠️)

Код написан в главе 5 шагах 1-3 без запуска. Пока владелец не пришлёт результат `:shared:data:testAndroidHostTest`, считать непроверенным:

- Ktor `3.6.1`: `HttpClient { }` без явного движка, `defaultRequest { url(...) }`, `install(ContentNegotiation) { json(...) }`, `install(HttpTimeout)`, `client.request(path) { ... }`, `bearerAuth`, `MockEngine`/`MockRequestHandler`, `TextContent` в тестах.
- kotlinx.serialization `1.9.0`: `JsonUnquotedLiteral`, `JsonDecoder.decodeJsonElement()`, `@Serializable(with = ...)` на свойстве.

## 3. Вставить из документации (владелец)

Скопируйте сюда выдержки (с версией и датой) и обновляйте при смене версий:

- [ ] Decompose: Quick start, Navigation, ChildStack, ChildSlot, Value и подписка на него; сохранение состояния (`StateKeeper`), `retainedComponent` на Android.
- [ ] Decompose: Web (история браузера) и iOS (back gesture) при необходимости.
- [ ] Ktor Client: Auth plugin (не используем), Logging, MockEngine.
- [ ] SQLDelight: Multiplatform, Web, Coroutines extensions, миграции (`.sqm`, `verifyMigrations`).
- [ ] Koin: `koin-core`, модули, `startKoin` на платформах.
- [ ] Compose Multiplatform for web: статус стабильности и поддерживаемые браузеры.
