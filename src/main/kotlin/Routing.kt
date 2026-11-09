package com.kaloo.backend

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val username: String? = null,
    val role: String? = null,
    val message: String? = null
)

fun Application.configureRouting() {
    routing {
        post("/login") {
            val request = call.receive<LoginRequest>()
            val cleanUser = request.username.trim()
            val cleanPass = request.password.trim()

            val userFound = transaction {
                UsersTable.selectAll()
                    .where { (UsersTable.username eq cleanUser) and (UsersTable.password eq cleanPass) }
                    .map {
                        AuthResponse(
                            success = true,
                            username = it[UsersTable.username],
                            role = it[UsersTable.role]
                        )
                    }
                    .singleOrNull()
            }

            if (userFound != null) {
                call.respond(HttpStatusCode.OK, userFound)
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    AuthResponse(success = false, message = "Credenciales incorrectas")
                )
            }
        }
    }
}