package com.kaloo.routes

import com.kaloo.models.AdminPanelResponse
import com.kaloo.models.Establishment
import com.kaloo.models.LostItemRequest
import com.kaloo.repositories.EstablishmentRepository
import com.kaloo.repositories.StreakRepository
import com.kaloo.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Panel de administrador.
 *
 * En este apartado se visualizarA y gestionarA la informaciOn
 * del establecimiento.
 */
fun Application.configureAdminRoutes() {
    routing {
        route("/api/admin") {

            // Resumen del panel: tItulo + informaciOn del establecimiento
            get("/panel") {
                call.respond(
                    AdminPanelResponse(
                        title = "Panel de administrador",
                        establishment = EstablishmentRepository.get()
                    )
                )
            }

            // Visualizar la informaciOn del establecimiento
            get("/establishment") {
                call.respond(EstablishmentRepository.get())
            }

            // Rama "Objetos perdidos": telEfono del gerente (para el panel admin)
            get("/lost-and-found") {
                call.respond(EstablishmentRepository.getLostAndFound())
            }

            // Rachas de clientes (vista admin)
            get("/streaks") {
                call.respond(StreakRepository.listAll())
            }

            // Usuarios/roles (vista admin)
            get("/users") {
                val users = UserRepository.loadUsers().map { mapOf("username" to it.username, "role" to it.role) }
                call.respond(users)
            }

            // Gestionar: actualizar la informaciOn del establecimiento
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

        // Rama pUblica "Objetos perdidos" (la ve el cliente sin ser admin)
        get("/api/lost-and-found") {
            call.respond(EstablishmentRepository.getLostAndFound())
        }

        // Reportar objeto perdido/encontrado (cliente)
        post("/api/lost-and-found/items") {
            val req = call.receive<LostItemRequest>()
            if (req.name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "name requerido"))
                return@post
            }
            val item = EstablishmentRepository.addLostItem(
                name = req.name.trim(),
                description = req.description.trim(),
                color = req.color?.trim()?.takeIf { it.isNotBlank() },
                imageBase64 = req.imageBase64?.takeIf { it.isNotBlank() }
            )
            call.respond(HttpStatusCode.Created, item)
        }
    }
}
