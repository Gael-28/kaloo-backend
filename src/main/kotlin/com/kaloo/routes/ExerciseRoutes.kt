package com.kaloo.routes

import com.kaloo.models.ExerciseRequest
import com.kaloo.services.ExerciseService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.exerciseRoutes() {
    post("/api/exercises") {
        val req = call.receive<ExerciseRequest>()
        call.respond(ExerciseService.recommend(req))
    }
}
