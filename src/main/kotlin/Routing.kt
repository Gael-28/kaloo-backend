package com.kaloo

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


@Serializable
data class NutritionRequest(
    val weight: Float,
    val height: Float
)

@Serializable
data class NutritionResponse(
    val bmi: Float,
    val recommendation: String
)

@Serializable
data class ExerciseRequest(
    val goal: String
)

@Serializable
data class ExerciseResponse(
    val goal: String,
    val routine: List<String>
)

@Serializable
data class CoachMessageRequest(
    val username: String,
    val coachName: String,
    val message: String
)

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String
)

// --- Definición de Rutas ---

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Servidor Ktor ProyectoGym Activo")
        }

        // Endpoint: Cálculo de IMC y Plan Nutricional
        post("/api/nutrition") {
            val req = call.receive<NutritionRequest>()
            val heightInMeters = req.height / 100f
            val bmi = req.weight / (heightInMeters * heightInMeters)

            val recommendation = when {
                bmi < 18.5f -> "Bajo peso: Aumenta tu ingesta calórica diaria priorizando proteínas y carbohidratos complejos."
                bmi in 18.5f..24.9f -> "Peso saludable: Mantén una dieta balanceada con 2g de proteína por kg y grasas saludables."
                bmi in 25.0f..29.9f -> "Sobrepeso: Mantén un ligero déficit calórico y ajusta la ingesta de carbohidratos en días de descanso."
                else -> "Obesidad: Prioriza un déficit calórico moderado, consumo de fibra y mantén hidratación constante."
            }

            call.respond(NutritionResponse(bmi = bmi, recommendation = recommendation))
        }

        // Endpoint: Recomendación de Ejercicios por Objetivo
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

        // Endpoint: Enviar mensaje al Entrenador
        post("/api/coaches/message") {
            val req = call.receive<CoachMessageRequest>()
            // Aquí en un futuro se guardarían los mensajes en base de datos o lista global
            call.respond(
                ApiResponse(
                    success = true,
                    message = "Mensaje enviado con éxito al entrenador '${req.coachName}'."
                )
            )
        }
    }
}