package com.kaloo.config

/**
 * Configuración central de la aplicación.
 * Los valores se leen de variables de entorno (o se pueden cargar
 * desde un archivo .env / application.yaml en el futuro).
 *
 * SQL Server ejemplo:
 *   DB_HOST=localhost
 *   DB_PORT=1433
 *   DB_NAME=KalooDb
 *   DB_USER=sa
 *   DB_PASSWORD=tu_contrasena
 */
object AppConfig {

    val serverHost: String = System.getenv("HOST") ?: "0.0.0.0"
    val serverPort: Int = (System.getenv("PORT") ?: "8080").toIntOrNull() ?: 8080

    // --- SQL Server ---
    val dbHost: String = System.getenv("DB_HOST") ?: "localhost"
    val dbPort: Int = (System.getenv("DB_PORT") ?: "1433").toIntOrNull() ?: 1433
    val dbName: String = System.getenv("DB_NAME") ?: "KalooDb"
    val dbUser: String = System.getenv("DB_USER") ?: "sa"
    val dbPassword: String = System.getenv("DB_PASSWORD") ?: ""

    /** JDBC URL lista para DriverManager.getConnection(). */
    val jdbcUrl: String
        get() = "jdbc:sqlserver://$dbHost:$dbPort;databaseName=$dbName;encrypt=false;trustServerCertificate=true"
}
