import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))

            implementation(libs.compose.ui)
        }
    }
}

detekt {
    buildUponDefaultConfig = true // правила по умолчанию + исключения из config/detekt/detekt.yml
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    source.setFrom("src") // все source sets KMP: commonMain, androidMain, тесты
    baseline = file("detekt-baseline.xml") // создаётся командой detektBaseline; пока файла нет, не используется
    parallel = true
    basePath.set(rootProject.projectDir)
}
