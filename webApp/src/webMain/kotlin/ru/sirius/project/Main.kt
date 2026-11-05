package ru.sirius.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // В браузере lifecycle тоже создаём вручную (как на iOS).
    val lifecycle = LifecycleRegistry()
    val root = createRoot(DefaultComponentContext(lifecycle))
    lifecycle.resume()

    ComposeViewport {
        App(root)
    }
}
