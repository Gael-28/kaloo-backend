package com.kaloo.config

import io.ktor.server.application.*
import java.sql.Connection
import java.sql.DriverManager

/**
 * Acceso a PostgreSQL vIa JDBC (compatible con Railway).
 */
object Database {

    init {
        runCatching { Class.forName("org.postgresql.Driver") }
    }

    fun connect(): Connection =
        DriverManager.getConnection(AppConfig.jdbcUrl, AppConfig.dbUser, AppConfig.dbPassword)

    fun healthCheck(): Boolean = try {
        connect().use { it.isValid(2) }
    } catch (_: Exception) {
        false
    }

    fun initSchema() {
        connect().use { conn ->
            conn.createStatement().use { st ->
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS establishments (
                        id SERIAL PRIMARY KEY,
                        name TEXT NOT NULL,
                        address TEXT NOT NULL,
                        phone TEXT NOT NULL DEFAULT '',
                        email TEXT NOT NULL DEFAULT '',
                        opening_hours TEXT NOT NULL DEFAULT '',
                        description TEXT,
                        manager_name TEXT NOT NULL DEFAULT '',
                        manager_phone TEXT NOT NULL DEFAULT '',
                        lost_and_found_note TEXT
                    )
                    """.trimIndent()
                )
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS user_streaks (
                        username TEXT PRIMARY KEY,
                        current_streak INT NOT NULL DEFAULT 0,
                        longest_streak INT NOT NULL DEFAULT 0,
                        total_check_ins INT NOT NULL DEFAULT 0,
                        last_check_in DATE
                    )
                    """.trimIndent()
                )
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS lost_items (
                        id SERIAL PRIMARY KEY,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        color TEXT,
                        image_base64 TEXT,
                        date_found DATE,
                        status TEXT NOT NULL DEFAULT 'ENCONTRADO'
                    )
                    """.trimIndent()
                )
                st.execute("ALTER TABLE lost_items ADD COLUMN IF NOT EXISTS color TEXT")
            }

            conn.prepareStatement("SELECT COUNT(*) FROM establishments").use { ps ->
                ps.executeQuery().use { rs ->
                    if (rs.next() && rs.getInt(1) == 0) {
                        conn.prepareStatement(
                            """
                            INSERT INTO establishments
                            (name, address, phone, email, opening_hours, description, manager_name, manager_phone, lost_and_found_note)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent()
                        ).use { ins ->
                            ins.setString(1, "Kaloo Gym")
                            ins.setString(2, "Calle Ejemplo 123")
                            ins.setString(3, "555-0100")
                            ins.setString(4, "contacto@kaloo.example")
                            ins.setString(5, "Lun a Vie 06:00-22:00, SAb 08:00-14:00")
                            ins.setString(6, "Establecimiento deportivo")
                            ins.setString(7, "Gerente Kaloo")
                            ins.setString(8, "555-0111")
                            ins.setString(9, "Para checar objetos perdidos, llama al gerente en horario del establecimiento.")
                            ins.executeUpdate()
                        }
                    }
                }
            }

            conn.prepareStatement("SELECT COUNT(*) FROM lost_items").use { ps ->
                ps.executeQuery().use { rs ->
                    if (rs.next() && rs.getInt(1) == 0) {
                        conn.prepareStatement(
                            """
                            INSERT INTO lost_items (name, description, color, image_base64, date_found, status)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """.trimIndent()
                        ).use { ins ->
                            val samples = listOf(
                                arrayOf("Mancuerna", "Mancuerna encontrada en zona de pesas.", "Negro", "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAA4SURBVDhPY/jw4cN/SvCoAUgGZGZmwjGyAmSMTQ1WAxoaGrBiZDXD3QCYGDrGpgZuALl41IAP/wH3Lna6uuiI8AAAAABJRU5ErkJggg==", "2026-11-20", "ENCONTRADO"),
                                arrayOf("Disco de barra", "Disco de peso encontrado cerca de racks.", "Gris", "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAABfSURBVDhPzc9BCkAhCIThrustPLI3KCYY8Jlu8i1a/Itw+KBhZrNKRHbZjT0KqOqOAN8obg/AjzMA+f0HiMMKQP8BHN3WB9pfeAtAflQBfn8AiMMMiNsUYASyG2sCNhcHRxWylAslpwAAAABJRU5ErkJggg==", "2026-11-22", "ENCONTRADO"),
                                arrayOf("Toalla deportiva", "Toalla gris encontrada en vestidores.", "Gris", "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAA+SURBVDhPY/jw4cN/SjB1DWhoaCAKI+uhjQFfv37FiulrwMqVK1EwyQZgw6MGkGAAIYysh7oGkIMpNODDfwDJL2/uXZ7RYgAAAABJRU5ErkJggg==", "2026-11-24", "ENCONTRADO")
                            )
                            for (s in samples) {
                                ins.setString(1, s[0]); ins.setString(2, s[1]); ins.setString(3, s[2]); ins.setString(4, s[3])
                                ins.setDate(5, java.sql.Date.valueOf(s[4]))
                                ins.setString(6, s[5])
                                ins.executeUpdate()
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Application.configureDatabase() {
    runCatching {
        Database.initSchema()
        environment.log.info("PostgreSQL conectado e inicializado (${AppConfig.dbHost}:${AppConfig.dbPort}/${AppConfig.dbName})")
    }.onFailure {
        environment.log.error("No se pudo inicializar PostgreSQL: ${it.message}", it)
    }
}
