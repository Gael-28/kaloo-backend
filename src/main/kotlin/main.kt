package com.kaloo

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class User(val username: String, val password: String, val role: String)

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) { json() }

        routing {
            post("/login") {
                val request = call.receive<LoginRequest>()

                // Lee el archivo users.json desde src/main/resources
                val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("users.json")
                if (stream == null) {
                    call.respond(AuthResponse(success = false, message = "Archivo users.json no encontrado en recursos"))
                    return@post
                }

                val jsonText = stream.bufferedReader().use { it.readText() }
                val users = Json.decodeFromString<List<User>>(jsonText)
                val matchedUser = users.find { it.username == request.username && it.password == request.password }

                if (matchedUser != null) {
                    call.respond(AuthResponse(success = true, username = matchedUser.username, role = matchedUser.role))
                } else {
                    call.respond(AuthResponse(success = false, message = "Credenciales incorrectas"))
                }
            }
        }
    }.start(wait = true)
}