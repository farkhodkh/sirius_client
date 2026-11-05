package ru.sirius.project.shared.domain

interface AuthRepository {
    /** Есть ли валидная сессия. От этого зависит стартовый экран. */
    val isSignedIn: Boolean

    /** Вход. В главе 5 здесь появится OAuth2 Authorization Code + PKCE через системный браузер. */
    suspend fun signIn()

    suspend fun signOut()
}
