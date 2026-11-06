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
    // Obtener la API Key desde application.yaml
    val apiKey = environment.config.propertyOrNull("gemini.apiKey")?.getString() ?: ""
    val geminiService = GeminiService(apiKey)

    routing {
        get("/") {
            call.respondText("Servidor Ktor ProyectoGym Activo")
        }

        // Endpoint: Login
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

        // Endpoint: Nutrición con IA Real
        post("/api/nutrition") {
            val req = call.receive<NutritionRequest>()
            val heightM = req.height / 100f
            val bmi = req.weight / (heightM * heightM)

            val prompt = """
                Actúa como un nutriólogo deportivo profesional.
                El usuario pesa ${req.weight} kg, mide ${req.height} cm y tiene un IMC de $bmi.
                Proporciona un consejo nutricional personalizado y conciso (máximo 3 oraciones), 
                incluyendo distribución de macronutrientes recomendada.
            """.trimIndent()

            val aiRecommendation = geminiService.generateContent(prompt)

            call.respond(NutritionResponse(bmi = bmi, recommendation = aiRecommendation))
        }

        // Endpoint: Ejercicios con IA Real
        post("/api/exercises") {
            val req = call.receive<ExerciseRequest>()

            val prompt = """
                Actúa como un entrenador personal experto de gimnasio.
                El usuario tiene el siguiente objetivo: '${req.goal}'.
                Genera una lista exacta de 4 ejercicios recomendados con series y repeticiones.
                Devuelve únicamente los ejercicios en líneas separadas sin introducciones ni viñetas.
            """.trimIndent()

            val rawResponse = geminiService.generateContent(prompt)
            val routineList = rawResponse.lines()
                .map { it.replace("*", "").trim() }
                .filter { it.isNotBlank() }

            call.respond(ExerciseResponse(goal = req.goal, routine = routineList))
        }

        // Endpoint: Mensajes a Entrenador
        post("/api/coaches/message") {
            val req = call.receive<CoachMessageRequest>()
            call.respond(ApiResponse(success = true, message = "Mensaje enviado exitosamente al entrenador '${req.coachName}'."))
        }
    }
}