package com.kaloo.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Punto de entrada de rutas.
 * Registra el endpoint raíz y delega en los grupos por módulo.
 * (Este archivo reemplaza al antiguo Routing.kt de com.kaloo)
 */
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Servidor Ktor ProyectoGym Activo")
        }
        authRoutes()
        nutritionRoutes()
        exerciseRoutes()
        coachRoutes()
        streakRoutes()
    }
}
