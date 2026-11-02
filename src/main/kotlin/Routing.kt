package com.kaloo

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

// --- Modelos de Autenticación ---
@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val role: String? = null,
    val username: String? = null
)

// --- Modelos para Cliente ---
@Serializable
data class NutritionRequest(val weight: Float, val height: Float)

@Serializable
data class NutritionResponse(val bmi: Float, val recommendation: String)

@Serializable
data class ExerciseRequest(val goal: String)

@Serializable
data class ExerciseResponse(val goal: String, val routine: List<String>)

@Serializable
data class CoachMessageRequest(val username: String, val coachName: String, val message: String)

@Serializable
data class ApiResponse(val success: Boolean, val message: String)

// --- Enrutamiento ---
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Servidor Ktor ProyectoGym Activo")
        }

        // Endpoint de Inicio de Sesión
        post("/login") {
            val req = call.receive<LoginRequest>()
            when {
                req.username == "admin" && req.password == "123" -> {
                    call.respond(AuthResponse(success = true, role = "ADMINISTRADOR", username = "admin"))
                }
                req.username == "entrenador" && req.password == "123" -> {
                    call.respond(AuthResponse(success = true, role = "ENTRENADOR", username = "entrenador"))
                }
                req.username == "cliente" && req.password == "123" -> {
                    call.respond(AuthResponse(success = true, role = "CLIENTE", username = "cliente"))
                }
                else -> {
                    call.respond(AuthResponse(success = false, message = "Credenciales incorrectas"))
                }
            }
        }

        // Endpoint: Nutrición e IMC
        post("/api/nutrition") {
            val req = call.receive<NutritionRequest>()
            val heightM = req.height / 100f
            val bmi = req.weight / (heightM * heightM)
            val recommendation = when {
                bmi < 18.5f -> "Bajo peso: Incrementa la ingesta calórica con carbohidratos complejos y proteína."
                bmi in 18.5f..24.9f -> "Peso saludable: Mantén balance calórico y 2g de proteína por kg."
                bmi in 25.0f..29.9f -> "Sobrepeso: Realiza un ligero déficit calórico y ajusta carbohidratos."
                else -> "Obesidad: Mantén déficit calórico moderado, consumo de fibra y buena hidratación."
            }
            call.respond(NutritionResponse(bmi = bmi, recommendation = recommendation))
        }

        // Endpoint: Recomendación de Ejercicios
        post("/api/exercises") {
            val req = call.receive<ExerciseRequest>()
            val goalLower = req.goal.lowercase()
            val routine = when {
                goalLower.contains("fuerza") || goalLower.contains("masa") -> listOf(
                    "Sentadilla libre con barra - 4 series x 8-10 reps",
                    "Press de banca plano - 4 series x 8 reps",
                    "Remo con barra inclinado - 4 series x 10 reps",
                    "Press militar con mancuernas - 3 series x 12 reps"
                )
                goalLower.contains("grasa") || goalLower.contains("perder") -> listOf(
                    "Circuito HIIT en caminadora - 20 minutos",
                    "Zancadas con mancuernas - 4 series x 15 reps por pierna",
                    "Kettlebell Swings - 4 series x 20 reps",
                    "Plancha abdominal - 4 series x 45 segundos"
                )
                else -> listOf(
                    "Calentamiento articular - 10 minutos",
                    "Circuito general fullbody - 3 series x 12 reps",
                    "Estiramientos estáticos - 10 minutos"
                )
            }
            call.respond(ExerciseResponse(goal = req.goal, routine = routine))
        }

        // Endpoint: Mensaje a Entrenador
        post("/api/coaches/message") {
            val req = call.receive<CoachMessageRequest>()
            call.respond(ApiResponse(success = true, message = "Mensaje enviado exitosamente al entrenador '${req.coachName}'."))
        }
    }
}