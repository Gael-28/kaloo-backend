package com.kaloo.config

import java.net.URI

/**
 * Configuración central de la aplicación.
 * Soporta Railway PostgreSQL mediante DATABASE_URL o variables PG* / DB_*.
 */
object AppConfig {

    val serverHost: String = System.getenv("HOST") ?: "0.0.0.0"
    val serverPort: Int = (System.getenv("PORT") ?: "8080").toIntOrNull() ?: 8080

    private val databaseUrl: String? = System.getenv("DATABASE_URL")?.takeIf { it.isNotBlank() }

    private val parsedDb: DbConfig? = databaseUrl?.let { parseDatabaseUrl(it) }

    val dbHost: String = parsedDb?.host
        ?: System.getenv("PGHOST")
        ?: System.getenv("DB_HOST")
        ?: "localhost"
    val dbPort: Int = parsedDb?.port
        ?: System.getenv("PGPORT")?.toIntOrNull()
        ?: System.getenv("DB_PORT")?.toIntOrNull()
        ?: 5432
    val dbName: String = parsedDb?.name
        ?: System.getenv("PGDATABASE")
        ?: System.getenv("DB_NAME")
        ?: "kaloo"
    val dbUser: String = parsedDb?.user
        ?: System.getenv("PGUSER")
        ?: System.getenv("DB_USER")
        ?: "postgres"
    val dbPassword: String = parsedDb?.password
        ?: System.getenv("PGPASSWORD")
        ?: System.getenv("DB_PASSWORD")
        ?: ""

    /** JDBC URL para PostgreSQL (Railway/local). */
    val jdbcUrl: String
        get() = "jdbc:postgresql://$dbHost:$dbPort/$dbName?sslmode=${System.getenv("DB_SSLMODE") ?: "prefer"}"

    private data class DbConfig(
        val host: String,
        val port: Int,
        val name: String,
        val user: String,
        val password: String
    )

    private fun parseDatabaseUrl(url: String): DbConfig? = runCatching {
        val normalized = when {
            url.startsWith("postgres://") -> url.replaceFirst("postgres://", "postgresql://")
            url.startsWith("postgresql://") -> url
            else -> return@runCatching null
        }
        val uri = URI(normalized)
        val userInfo = uri.userInfo?.split(":", limit = 2)
        DbConfig(
            host = uri.host ?: return@runCatching null,
            port = if (uri.port == -1) 5432 else uri.port,
            name = uri.path?.trimStart('/')?.takeIf { it.isNotBlank() } ?: return@runCatching null,
            user = userInfo?.getOrNull(0) ?: "postgres",
            password = userInfo?.getOrNull(1) ?: ""
        )
    }.getOrNull()
}
