package com.kaloo.repositories

import com.kaloo.models.User
import kotlinx.serialization.json.Json

/**
 * Repositorio de usuarios.
 *
 * Por ahora lee src/main/resources/users.json (mismo comportamiento
 * que tenía main.kt). Cuando exista SQL Server, reemplazar por
 * consultas JDBC a la tabla Users.
 */
object UserRepository {

    private val json = Json { ignoreUnknownKeys = true }

    /** Usuarios de ejemplo (respaldo si no se encuentra users.json). */
    private val fallbackUsers = listOf(
        User("admin", "123", "ADMINISTRADOR"),
        User("entrenador", "123", "ENTRENADOR"),
        User("cliente", "123", "CLIENTE")
    )

    /** Carga los usuarios desde users.json; si falta, usa [fallbackUsers]. */
    fun loadUsers(): List<User> {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("users.json")
            ?: return fallbackUsers
        return try {
            val text = stream.bufferedReader().use { it.readText() }
            json.decodeFromString<List<User>>(text)
        } catch (e: Exception) {
            fallbackUsers
        }
    }

    /** Busca un usuario por usuario + contraseña (login por roles). */
    fun findByCredentials(username: String, password: String): User? =
        loadUsers().find { it.username == username && it.password == password }
}
