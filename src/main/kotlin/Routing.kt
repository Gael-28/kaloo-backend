package com.kaloo

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Servidor Ktor ProyectoGym Activo")
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()


                val usersJson = object {}.javaClass.classLoader.getResourceAsStream("users.json")
                    ?.bufferedReader()
                    ?.use { it.readText() }

                if (usersJson.isNullOrEmpty()) {
                    call.respond(AuthResponse(success = false, message = "No se encontro users.json en recursos"))
                    return@post
                }

                val users = Json.decodeFromString<List<User>>(usersJson)
                val foundUser = users.find { it.username == request.username && it.password == request.password }

                if (foundUser != null) {
                    call.respond(AuthResponse(success = true, username = foundUser.username, role = foundUser.role))
                } else {
                    call.respond(AuthResponse(success = false, message = "Usuario o contraseña incorrectos"))
                }
            } catch (e: Exception) {
                call.respond(AuthResponse(success = false, message = "Error en el servidor: ${e.message}"))
            }
        }
    }
}