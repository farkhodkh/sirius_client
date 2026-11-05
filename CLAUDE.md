# Sirius Client — KMP (Android, iOS, Web)

## Стек
Kotlin Multiplatform, Compose Multiplatform, Decompose (навигация и компоненты), Ktor Client, SQLDelight, Koin, kotlinx.serialization.
Версии только из gradle/libs.versions.toml. Не обновлять версии без явной просьбы.

## Модули и зависимости
ui → features → domain ← data. domain: чистый Kotlin, без Ktor/Compose/Decompose/SQLDelight/платформ.

## Команды
- Сборка: ./gradlew build
- Тесты: ./gradlew allTests
- Android: ./gradlew :androidApp:assembleDebug
- Web dev: ./gradlew :webApp:wasmJsBrowserDevelopmentRun
- iOS-фреймворк (проверка сборки): ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
- Тесты слоя: ./gradlew :shared:features:testAndroidHostTest (аналогично :shared:data:)
- iOS: сборка через Xcode (iosApp/iosApp.xcodeproj)

## Правила кода
- Логика экранов только в компонентах Decompose, @Composable без бизнес-логики.
- Состояние: sealed interface + StateFlow, события вверх колбэками.
- Деньги не в Double. Секреты, токены и ключи не логировать и не коммитить.
- Конфигурации навигации @Serializable.
- Изменение схемы SQLDelight = миграция, согласовать со мной.

## Процесс
- Сначала план, жди «ок», потом код.
- Не придумывай API: если не уверен в сигнатуре Decompose/Ktor/SQLDelight, скажи; положу документацию в docs/.
- После изменений запусти сборку и тесты и перечисли результат, не утверждай «работает» без запуска.
- Не добавляй зависимости без согласования.

## Backend
«Сириус»: auth-service (OAuth2 PKCE), order-service (/api/v1/orders, Idempotency-Key). Спецификация: docs/openapi-order.yaml.
- Клиент OAuth: `sirius-mobile` (публичный, PKCE обязателен, без секрета). Redirect URI: `world.sirius.app:/oauth2redirect`. Scope: `openid email orders.read orders.write`.
- Публичным клиентам refresh-токен НЕ выдаётся. Access-токен живёт 10 минут; по `401` клиент заново идёт в системный браузер (SSO-сессия не просит пароль повторно). Токен хранить только в памяти.
- `Idempotency-Key`: 8-64 символа, один ключ на «намерение» пользователя, при ретрае тот же.