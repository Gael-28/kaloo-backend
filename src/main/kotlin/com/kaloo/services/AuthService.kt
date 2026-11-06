package com.kaloo.services

import com.kaloo.models.AuthResponse
import com.kaloo.models.LoginRequest
import com.kaloo.repositories.UserRepository

/**
 * Lógica de inicio de sesión por roles.
 * Extraída del endpoint /login que estaba duplicado en main.kt y Routing.kt.
 */
object AuthService {

    fun login(request: LoginRequest): AuthResponse {
        val user = UserRepository.findByCredentials(request.username, request.password)
            ?: return AuthResponse(success = false, message = "Credenciales incorrectas")
        return AuthResponse(success = true, role = user.role, username = user.username)
    }
}
