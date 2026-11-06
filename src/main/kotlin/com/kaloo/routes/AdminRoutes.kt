package com.kaloo.routes

import com.kaloo.models.AdminPanelResponse
import com.kaloo.models.Establishment
import com.kaloo.repositories.EstablishmentRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Panel de administrador.
 *
 * En este apartado se visualizará y gestionará la información
 * del establecimiento.
 */
fun Application.configureAdminRoutes() {
    routing {
        route("/api/admin") {

            // Resumen del panel: título + información del establecimiento
            get("/panel") {
                call.respond(
                    AdminPanelResponse(
                        title = "Panel de administrador",
                        establishment = EstablishmentRepository.get()
                    )
                )
            }

            // Visualizar la información del establecimiento
            get("/establishment") {
                call.respond(EstablishmentRepository.get())
            }

            // Rama "Objetos perdidos": teléfono del gerente (para el panel admin)
            get("/lost-and-found") {
                call.respond(EstablishmentRepository.getLostAndFound())
            }

            // Gestionar: actualizar la información del establecimiento
            put("/establishment") {
                val updated = call.receive<Establishment>()
                if (updated.name.isBlank() || updated.address.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        AdminPanelResponse(
                            title = "Panel de administrador",
                            establishment = EstablishmentRepository.get()
                        )
                    )
                    return@put
                }
                call.respond(EstablishmentRepository.update(updated))
            }
        }

        // Rama pública "Objetos perdidos" (la ve el cliente sin ser admin)
        get("/api/lost-and-found") {
            call.respond(EstablishmentRepository.getLostAndFound())
        }
    }
}
