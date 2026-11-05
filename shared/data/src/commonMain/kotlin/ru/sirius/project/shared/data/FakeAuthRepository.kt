package ru.sirius.project.shared.data

import ru.sirius.project.shared.domain.AuthRepository

/** Временная реализация без сети: вход всегда успешен. Заменить реальной в главе 5. */
class FakeAuthRepository : AuthRepository {
    override var isSignedIn: Boolean = false
        private set

    override suspend fun signIn() {
        isSignedIn = true
    }

    override suspend fun signOut() {
        isSignedIn = false
    }
}
