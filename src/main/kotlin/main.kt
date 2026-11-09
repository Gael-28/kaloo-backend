package com.kaloo.backend

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert

fun main() {
    DatabaseFactory.init()

    // 1. Crear tabla users e insertar usuario admin si no existe
    transaction {
        SchemaUtils.create(UsersTable)

        val userExists = UsersTable.selectAll()
            .where { UsersTable.username eq "admin" }
            .count() > 0

        if (!userExists) {
            UsersTable.insert {
                it[username] = "admin"
                it[password] = "1234"
                it[role] = "ADMIN"
            }
            println("=========================================")
            println(">>> USUARIO 'admin' CREADO EN LA BASE DE DATOS")
            println("=========================================")
        }
    }

    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    // 2. Iniciar servidor Netty
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        routing {
            post("/login") {
                val request = call.receive<LoginRequest>()
                val cleanUser = request.username.trim()
                val cleanPass = request.password.trim()

                println("=========================================")
                println(">>> INTENTO LOGIN: user='$cleanUser' pass='$cleanPass'")

                val userFound = transaction {
                    UsersTable.selectAll()
                        .where { (UsersTable.username eq cleanUser) and (UsersTable.password eq cleanPass) }
                        .map {
                            AuthResponse(
                                success = true,
                                username = it[UsersTable.username],
                                role = it[UsersTable.role]
                            )
                        }
                        .singleOrNull()
                }

                println("<<< RESULTADO BD: $userFound")
                println("=========================================")

                if (userFound != null) {
                    call.respond(HttpStatusCode.OK, userFound)
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(success = false, message = "Credenciales incorrectas")
                    )
                }
            }
        }
    }.start(wait = true)
}