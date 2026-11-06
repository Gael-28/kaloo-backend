package com.kaloo.services

import com.kaloo.models.NutritionRequest
import com.kaloo.models.NutritionResponse

/** Plan nutricional básico según IMC (peso / estatura). */
object NutritionService {

    fun calculate(request: NutritionRequest): NutritionResponse {
        val heightM = request.height / 100f
        val bmi = request.weight / (heightM * heightM)
        val recommendation = when {
            bmi < 18.5f -> "Bajo peso: Incrementa la ingesta calórica con carbohidratos complejos y proteína."
            bmi in 18.5f..24.9f -> "Peso saludable: Mantén balance calórico y 2g de proteína por kg."
            bmi in 25.0f..29.9f -> "Sobrepeso: Realiza un ligero déficit calórico y ajusta carbohidratos."
            else -> "Obesidad: Mantén déficit calórico moderado, consumo de fibra y buena hidratación."
        }
        return NutritionResponse(bmi = bmi, recommendation = recommendation)
    }
}
