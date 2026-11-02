package com.kaloo

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class Calibracion(
    val username: String,
    val gravedadBase: Double,
    val umbralRepeticion: Double
)

@Serializable
data class UbicacionGym(
    val lat: Double,
    val lon: Double,
    val radioMetros: Int = 100
)

// Por ahora todo vive en memoria: se pierde al reiniciar el servidor.
// Cuando el equipo monte la base de datos, se cambia aqui.
private val calibraciones = ConcurrentHashMap<String, Calibracion>()

@Volatile
private var ubicacionGym: UbicacionGym? = null

fun Application.configureCalibracion() {
    routing {
        get("/calibracion/{username}") {
            val username = call.parameters["username"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val calibracion = calibraciones[username]
            if (calibracion == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(calibracion)
            }
        }

        put("/calibracion/{username}") {
            val username = call.parameters["username"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            // El usuario de la URL manda sobre el del cuerpo
            val calibracion = call.receive<Calibracion>().copy(username = username)
            calibraciones[username] = calibracion
            call.respond(calibracion)
        }

        get("/gimnasio/ubicacion") {
            val ubicacion = ubicacionGym
            if (ubicacion == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(ubicacion)
            }
        }

        put("/gimnasio/ubicacion") {
            val ubicacion = call.receive<UbicacionGym>()
            ubicacionGym = ubicacion
            call.respond(ubicacion)
        }
    }
}