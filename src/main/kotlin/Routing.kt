package com.kaloo.backend

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.and

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

            println("=========================================")
            println(">>> INTENTO LOGIN EN ROUTING: user='$cleanUser' pass='$cleanPass'")

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

            println("<<< RESULTADO BD: $userFound")
            println("=========================================")

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