package com.kaloo.backend

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.insert

fun main() {
    DatabaseFactory.init()

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

    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        configureRouting()
    }.start(wait = true)
}