package com.kaloo.repositories

import com.kaloo.models.Establishment
import com.kaloo.models.LostAndFoundInfo

/**
 * Repositorio en memoria del establecimiento.
 *
 * Por ahora se usa un objeto singleton con datos de ejemplo.
 * Cuando tengas base de datos o archivo JSON, reemplaza esta
 * implementación por la lectura/escritura real.
 */
object EstablishmentRepository {

    private var establishment = Establishment(
        name = "Kaloo Gym",
        address = "Calle Ejemplo 123",
        phone = "555-0100",
        email = "contacto@kaloo.example",
        openingHours = "Lun a Vie 06:00-22:00, Sáb 08:00-14:00",
        description = "Establecimiento deportivo",
        managerName = "Gerente Kaloo",
        managerPhone = "555-0111",
        lostAndFoundNote = "Para checar objetos perdidos, llama al gerente en horario del establecimiento."
    )

    // Visualizar la información del establecimiento
    fun get(): Establishment = establishment

    // Gestionar (actualizar) la información del establecimiento
    fun update(updated: Establishment): Establishment {
        establishment = updated
        return establishment
    }

    // Rama "Objetos perdidos": teléfono del gerente
    fun getLostAndFound(): LostAndFoundInfo = LostAndFoundInfo(
        establishmentName = establishment.name,
        managerName = establishment.managerName.ifBlank { "Gerente" },
        managerPhone = establishment.managerPhone.ifBlank { establishment.phone },
        note = establishment.lostAndFoundNote
    )
}
