package ru.sirius.project

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import platform.UIKit.UIViewController

// Имя с заглавной буквы задано контрактом с Swift: iosApp/ContentView.swift вызывает
// MainViewControllerKt.MainViewController(). Поэтому правила именования функций здесь отключены.
@Suppress("ktlint:standard:function-naming", "FunctionNaming")
fun MainViewController(): UIViewController {
    // На iOS жизненный цикл компонентов нужно создать и «оживить» (resume) вручную,
    // иначе компоненты не получат события lifecycle.
    val lifecycle = LifecycleRegistry()
    val root = createRoot(DefaultComponentContext(lifecycle))
    lifecycle.resume()

    return ComposeUIViewController { App(root) }
}
