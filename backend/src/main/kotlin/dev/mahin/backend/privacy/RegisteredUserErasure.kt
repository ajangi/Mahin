package dev.mahin.backend.privacy

import java.util.UUID

/** Boundary for account deletion erasure; enables test doubles without mocking final classes. */
fun interface RegisteredUserErasure {
    fun eraseRegisteredUser(userId: UUID)
}
