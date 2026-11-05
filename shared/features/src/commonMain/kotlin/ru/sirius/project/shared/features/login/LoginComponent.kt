package ru.sirius.project.shared.features.login

import com.arkivanov.decompose.ComponentContext

class LoginComponent(
    componentContext: ComponentContext,
    private val onSignIn: () -> Unit,
    private val onOpen: (String) -> Unit,
)