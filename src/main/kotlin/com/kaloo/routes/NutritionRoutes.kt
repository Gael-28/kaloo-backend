package com.kaloo.routes

import com.kaloo.models.NutritionRequest
import com.kaloo.services.NutritionService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.nutritionRoutes() {
    post("/api/nutrition") {
        val req = call.receive<NutritionRequest>()
        call.respond(NutritionService.calculate(req))
    }
}
