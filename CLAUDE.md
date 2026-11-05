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
- Форматирование (Spotless + ktlint): ./gradlew spotlessApply (исправить), ./gradlew spotlessCheck (проверить)
- Линтер (detekt): ./gradlew detekt; baseline при необходимости: ./gradlew detektBaseline

## Правила кода
- Логика экранов только в компонентах Decompose, @Composable без бизнес-логики.
- Состояние: sealed interface + StateFlow, события вверх колбэками.
- Деньги не в Double. Секреты, токены и ключи не логировать и не коммитить.
- Конфигурации навигации @Serializable.
- Стиль задан в .editorconfig (ktlint_official, строка до 140). Не оставляй wildcard-импортов. Код, который агент пишет, должен проходить spotlessCheck и detekt; в конце шага перечисли эти команды среди команд проверки.
- Правила detekt: config/detekt/detekt.yml. Не отключай правило и не добавляй @Suppress без согласования; обоснованное исключение записывай в detekt.yml с комментарием.
- Изменение схемы SQLDelight = миграция, согласовать со мной.

## Процесс
- Сначала план, жди «ок», потом код. «Ок» = явное согласие в чате («ок», «да», «делай»). Ответ «без предпочтений» на вопрос с вариантами = согласие с вариантом, помеченным (Recommended); принятые умолчания перечисли в ответе.
- Режим «агент пишет, владелец запускает» (действует, пока владелец не скажет иначе): НЕ запускай Gradle, тесты, эмуляторы и симуляторы. После каждого шага выдай точные команды запуска и ожидаемый результат; владелец пришлёт вывод, ты исправишь.
- Размер шага: один слой или один компонент, не больше ~8 файлов. Потом запуск владельцем и только после зелёного результата следующий шаг.
- Не придумывай API: опирайся на docs/decompose-api.md (там помечено, что проверено сборкой) и документацию в docs/. Если не уверен в сигнатуре Decompose/Ktor/SQLDelight/kotlinx, пометь «⚠️ не проверено» и скажи, что именно сверить.
- Не пиши «работает», пока владелец не прислал результат запуска. Пиши «написано, не запускалось».
- Не добавляй зависимости без согласования; в конце шага перечисляй добавленные библиотеки и версии.
- В конце зелёного шага предложи текст коммита; коммитит владелец (или ты по его явной просьбе).
- После каждого шага добавь запись в docs/ai-log.md (задача, что исправлено, какое правило добавлено).

## Backend
«Сириус»: auth-service (OAuth2 PKCE), order-service (/api/v1/orders, Idempotency-Key). Спецификация: docs/openapi-order.yaml.
- Клиент OAuth: `sirius-mobile` (публичный, PKCE обязателен, без секрета). Redirect URI: `world.sirius.app:/oauth2redirect`. Scope: `openid email orders.read orders.write`.
- Публичным клиентам refresh-токен НЕ выдаётся. Access-токен живёт 10 минут; по `401` клиент заново идёт в системный браузер (SSO-сессия не просит пароль повторно). Токен хранить только в памяти.
- `Idempotency-Key`: 8-64 символа, один ключ на «намерение» пользователя, при ретрае тот же.