package ru.sirius.project.shared.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Сириус", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        when (val s = state) {
            LoginState.Loading -> {
                CircularProgressIndicator()
            }

            LoginState.Idle -> {
                Button(onClick = component::onSignInClick) { Text("Войти") }
            }

            is LoginState.Error -> {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = component::onSignInClick) { Text("Повторить") }
            }
        }
    }
}
