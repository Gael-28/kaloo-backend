package com.kaloo.services

import com.kaloo.models.ExerciseRequest
import com.kaloo.models.ExerciseResponse

/** Recomendación de ejercicios según objetivo (placeholder de la IA). */
object ExerciseService {

    fun recommend(request: ExerciseRequest): ExerciseResponse {
        val goalLower = request.goal.lowercase()
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
        return ExerciseResponse(goal = request.goal, routine = routine)
    }
}
