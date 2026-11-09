package com.kaloo.backend

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
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

fun main() {
    // 1. Inicializar conexión a PostgreSQL en Railway
    DatabaseFactory.init()

    // 2. Puerto dinámico para Railway
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    // 3. Servidor Ktor único
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        routing {
            post("/login") {
                val request = call.receive<LoginRequest>()
                val cleanUsername = request.username.trim()
                val cleanPassword = request.password.trim()

                println("--> Intento de login: usuario='$cleanUsername', password='$cleanPassword'")

                val userFound = transaction {
                    UsersTable.selectAll()
                        .where { (UsersTable.username eq cleanUsername) and (UsersTable.password eq cleanPassword) }
                        .map {
                            AuthResponse(
                                success = true,
                                username = it[UsersTable.username],
                                role = it[UsersTable.role]
                            )
                        }
                        .singleOrNull()
                }

                println("<-- Resultado BD: $userFound")

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
    }.start(wait = true)
}