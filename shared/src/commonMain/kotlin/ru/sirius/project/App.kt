package ru.sirius.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ru.sirius.project.shared.features.root.RootComponent
import ru.sirius.project.shared.features.root.RootContent

@Composable
fun App(root: RootComponent) {
    MaterialTheme {
        RootContent(root)
    }
}
