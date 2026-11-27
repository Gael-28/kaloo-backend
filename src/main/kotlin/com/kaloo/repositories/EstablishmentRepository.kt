package com.kaloo.repositories

import com.kaloo.config.Database
import com.kaloo.models.Establishment
import com.kaloo.models.LostAndFoundInfo
import com.kaloo.models.LostItem

/**
 * Repositorio de establecimiento y objetos perdidos. Usa PostgreSQL (Railway) con fallback en memoria.
 */
object EstablishmentRepository {

    private var memoryEstablishment = Establishment(
        name = "Kaloo Gym",
        address = "Calle Ejemplo 123",
        phone = "555-0100",
        email = "contacto@kaloo.example",
        openingHours = "Lun a Vie 06:00-22:00, SAb 08:00-14:00",
        description = "Establecimiento deportivo",
        managerName = "Gerente Kaloo",
        managerPhone = "555-0111",
        lostAndFoundNote = "Para checar objetos perdidos, llama al gerente en horario del establecimiento."
    )

    private val memoryItems = mutableListOf(
        LostItem(1, "Mancuerna", "Mancuerna encontrada en zona de pesas.", "Negro", "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAA4SURBVDhPY/jw4cN/SvCoAUgGZGZmwjGyAmSMTQ1WAxoaGrBiZDXD3QCYGDrGpgZuALl41IAP/wH3Lna6uuiI8AAAAABJRU5ErkJggg==", "2026-11-20", "ENCONTRADO"),
        LostItem(2, "Disco de barra", "Disco de peso encontrado cerca de racks.", "Gris", "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAABfSURBVDhPzc9BCkAhCIThrustPLI3KCYY8Jlu8i1a/Itw+KBhZrNKRHbZjT0KqOqOAN8obg/AjzMA+f0HiMMKQP8BHN3WB9pfeAtAflQBfn8AiMMMiNsUYASyG2sCNhcHRxWylAslpwAAAABJRU5ErkJggg==", "2026-11-22", "ENCONTRADO"),
        LostItem(3, "Toalla deportiva", "Toalla gris encontrada en vestidores.", "Gris", "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAA+SURBVDhPY/jw4cN/SjB1DWhoaCAKI+uhjQFfv37FiulrwMqVK1EwyQZgw6MGkGAAIYysh7oGkIMpNODDfwDJL2/uXZ7RYgAAAABJRU5ErkJggg==", "2026-11-24", "ENCONTRADO")
    )

    fun get(): Establishment = runCatching {
        Database.connect().use { conn ->
            conn.prepareStatement(
                "SELECT name, address, phone, email, opening_hours, description, manager_name, manager_phone, lost_and_found_note FROM establishments ORDER BY id LIMIT 1"
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    if (rs.next()) Establishment(
                        name = rs.getString("name"),
                        address = rs.getString("address"),
                        phone = rs.getString("phone") ?: "",
                        email = rs.getString("email") ?: "",
                        openingHours = rs.getString("opening_hours") ?: "",
                        description = rs.getString("description"),
                        managerName = rs.getString("manager_name") ?: "",
                        managerPhone = rs.getString("manager_phone") ?: "",
                        lostAndFoundNote = rs.getString("lost_and_found_note")
                    ) else memoryEstablishment
                }
            }
        }
    }.getOrElse { memoryEstablishment }

    fun update(updated: Establishment): Establishment = runCatching {
        Database.connect().use { conn ->
            val existingId = conn.prepareStatement("SELECT id FROM establishments ORDER BY id LIMIT 1").use { ps ->
                ps.executeQuery().use { rs -> if (rs.next()) rs.getInt("id") else null }
            }
            if (existingId == null) {
                conn.prepareStatement(
                    """INSERT INTO establishments (name, address, phone, email, opening_hours, description, manager_name, manager_phone, lost_and_found_note)
                       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""".trimIndent()
                ).use { ps -> ps.bindEstablishment(updated); ps.executeUpdate() }
            } else {
                conn.prepareStatement(
                    """UPDATE establishments SET name=?, address=?, phone=?, email=?, opening_hours=?, description=?, manager_name=?, manager_phone=?, lost_and_found_note=? WHERE id=?""".trimIndent()
                ).use { ps ->
                    ps.bindEstablishment(updated)
                    ps.setInt(10, existingId)
                    ps.executeUpdate()
                }
            }
            memoryEstablishment = updated
            updated
        }
    }.getOrElse {
        memoryEstablishment = updated
        updated
    }

    fun getLostAndFound(): LostAndFoundInfo {
        val est = get()
        val items = runCatching {
            Database.connect().use { conn ->
                conn.prepareStatement(
                    "SELECT id, name, description, color, image_base64, date_found, status FROM lost_items ORDER BY id DESC"
                ).use { ps ->
                    ps.executeQuery().use { rs ->
                        buildList {
                            while (rs.next()) add(
                                LostItem(
                                    id = rs.getInt("id"),
                                    name = rs.getString("name"),
                                    description = rs.getString("description") ?: "",
                                    color = rs.getString("color"),
                                    imageBase64 = rs.getString("image_base64"),
                                    dateFound = rs.getDate("date_found")?.toLocalDate()?.toString(),
                                    status = rs.getString("status") ?: "ENCONTRADO"
                                )
                            )
                        }
                    }
                }
            }
        }.getOrElse { memoryItems.toList() }

        return LostAndFoundInfo(
            establishmentName = est.name,
            managerName = est.managerName.ifBlank { "Gerente" },
            managerPhone = est.managerPhone.ifBlank { est.phone },
            note = est.lostAndFoundNote,
            items = items
        )
    }

    fun addLostItem(name: String, description: String, color: String?, imageBase64: String?): LostItem {
        val today = java.time.LocalDate.now().toString()
        return runCatching {
            Database.connect().use { conn ->
                conn.prepareStatement(
                    "INSERT INTO lost_items (name, description, color, image_base64, date_found, status) VALUES (?, ?, ?, ?, ?, ?) RETURNING id"
                ).use { ps ->
                    ps.setString(1, name)
                    ps.setString(2, description)
                    ps.setString(3, color)
                    ps.setString(4, imageBase64)
                    ps.setDate(5, java.sql.Date.valueOf(today))
                    ps.setString(6, "ENCONTRADO")
                    ps.executeQuery().use { rs ->
                        rs.next()
                        val id = rs.getInt("id")
                        LostItem(id, name, description, color, imageBase64, today, "ENCONTRADO")
                    }
                }
            }
        }.getOrElse {
            val id = (memoryItems.maxOfOrNull { it.id } ?: 0) + 1
            val item = LostItem(id, name, description, color, imageBase64, today, "ENCONTRADO")
            memoryItems.add(0, item)
            item
        }
    }

    private fun java.sql.PreparedStatement.bindEstablishment(e: Establishment) {
        setString(1, e.name)
        setString(2, e.address)
        setString(3, e.phone)
        setString(4, e.email)
        setString(5, e.openingHours)
        setString(6, e.description)
        setString(7, e.managerName)
        setString(8, e.managerPhone)
        setString(9, e.lostAndFoundNote)
    }
}
