package com.kaloo.backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.insert
import java.net.URI

// Definición de la tabla de usuarios en PostgreSQL
object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 100)
    val role = varchar("role", 20)

    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun init() {
        val database = Database.connect(createHikariDataSource())

        transaction(database) {
            // Crea la tabla automáticamente si no existe en Postgres
            SchemaUtils.create(UsersTable)

            // Insertar usuario de prueba si la tabla está vacía
            if (UsersTable.selectAll().empty()) {
                UsersTable.insert {
                    it[username] = "admin"
                    it[password] = "1234"
                    it[role] = "admin"
                }
            }
        }
    }

    private fun createHikariDataSource(): HikariDataSource {
        val rawDbUrl = System.getenv("DATABASE_URL")
            ?: "jdbc:postgresql://localhost:5432/postgres"

        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"

            // Convertir la URL que entrega Railway (postgres://...) al formato JDBC (jdbc:postgresql://...)
            if (rawDbUrl.startsWith("postgres://") || rawDbUrl.startsWith("postgresql://")) {
                val dbUri = URI(rawDbUrl)
                val user = dbUri.userInfo?.split(":")?.get(0) ?: ""
                val pass = dbUri.userInfo?.split(":")?.get(1) ?: ""
                val port = if (dbUri.port != -1) dbUri.port else 5432

                jdbcUrl = "jdbc:postgresql://${dbUri.host}:$port${dbUri.path}"
                username = user
                password = pass
            } else {
                jdbcUrl = rawDbUrl
            }

            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }
}