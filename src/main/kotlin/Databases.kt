package com.kaloo.backend

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val host = System.getenv("PGHOST")
    val port = System.getenv("PGPORT")
    val database = System.getenv("PGDATABASE")
    val user = System.getenv("PGUSER")
    val password = System.getenv("PGPASSWORD")

    Database.connect(
        url = "jdbc:postgresql://$host:$port/$database",
        user = user ?: "",
        driver = "org.postgresql.Driver",
        password = password ?: ""
    )
}