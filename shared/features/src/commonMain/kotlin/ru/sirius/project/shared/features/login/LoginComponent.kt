package ru.sirius.project.shared.features.login

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.sirius.project.shared.domain.AuthRepository
import kotlin.coroutines.cancellation.CancellationException

class LoginComponent(
    componentContext: ComponentContext,
    private val auth: AuthRepository,
    private val onSignedIn: () -> Unit,
) : ComponentContext by componentContext {
    private val scope = coroutineScope()
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onSignInClick() {
        if (_state.value is LoginState.Loading) return
        _state.value = LoginState.Loading
        scope.launch {
            try {
                auth.signIn()
                onSignedIn()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "Ошибка входа")
            }
        }
    }
}

sealed interface LoginState {
    data object Idle : LoginState

    data object Loading : LoginState

    data class Error(
        val message: String,
    ) : LoginState
}
