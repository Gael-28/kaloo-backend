package com.kaloo.services

import com.kaloo.models.NutritionRequest
import com.kaloo.models.NutritionResponse

/** Plan nutricional bAsico segUn IMC (peso / estatura). */
object NutritionService {

    fun calculate(request: NutritionRequest): NutritionResponse {
        val heightM = request.height / 100f
        val bmi = request.weight / (heightM * heightM)
        val recommendation = when {
            bmi < 18.5f -> "Bajo peso: Incrementa la ingesta calOrica con carbohidratos complejos y proteIna."
            bmi in 18.5f..24.9f -> "Peso saludable: MantEn balance calOrico y 2g de proteIna por kg."
            bmi in 25.0f..29.9f -> "Sobrepeso: Realiza un ligero dEficit calOrico y ajusta carbohidratos."
            else -> "Obesidad: MantEn dEficit calOrico moderado, consumo de fibra y buena hidrataciOn."
        }
        return NutritionResponse(bmi = bmi, recommendation = recommendation)
    }
}
