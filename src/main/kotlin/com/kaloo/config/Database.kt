package com.kaloo.config

import io.ktor.server.application.*
import java.sql.Connection
import java.sql.DriverManager

/**
 * Acceso a SQL Server vía JDBC (java.sql).
 *
 * Por ahora no hay tablas creadas: este objeto solo centraliza la
 * configuración y la conexión. Cuando se agreguen las tablas, aquí
 * irá el DataSource / Exposed Database.
 *
 * Driver requerido (ver build.gradle.kts):
 *   com.microsoft.sqlserver:mssql-jdbc
 */
object Database {

    init {
        // Registra el driver de SQL Server si está en el classpath.
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
    }

    /** Abre una conexión JDBC nueva con los valores de [AppConfig]. */
    fun connect(): Connection =
        DriverManager.getConnection(AppConfig.jdbcUrl, AppConfig.dbUser, AppConfig.dbPassword)

    /** Prueba rápida de conexión (útil para /health). */
    fun healthCheck(): Boolean = try {
        connect().use { it.isValid(2) }
    } catch (e: Exception) {
        false
    }
}

/** Verifica que SQL Server responde. */
fun Application.configureDatabase() {
    // Placeholder: aquí se puede inicializar Exposed / pools cuando existan tablas.
    // Por ahora el servidor arranca sin BD (los repositorios usan memoria).
}
