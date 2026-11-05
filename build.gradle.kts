plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.detekt) apply false // применяется в модулях, настройки в config/detekt/detekt.yml
    alias(libs.plugins.spotless) // форматирование всего репозитория из корня
}

// Форматирование (ktlint через Spotless). Правила стиля в .editorconfig.
//   ./gradlew spotlessApply   исправить форматирование
//   ./gradlew spotlessCheck   только проверить (входит в ./gradlew check и build)
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", "**/.gradle/**", "**/kotlin-js-store/**")
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint(libs.versions.ktlint.get())
    }
}
