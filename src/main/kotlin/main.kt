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

@Serializable
data class User(val username: String, val password: String, val role: String)

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
                // Aquí irá la validación con la base de datos
                call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
            }
        }
    }.start(wait = true)
}