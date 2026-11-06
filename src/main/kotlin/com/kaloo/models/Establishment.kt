package com.kaloo.models

import kotlinx.serialization.Serializable

// --- Modelo del establecimiento ---
// En este apartado se visualizará y gestionará la información del establecimiento
@Serializable
data class Establishment(
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
    val openingHours: String,
    val description: String? = null,
    // Rama "Objetos perdidos": teléfono del gerente para checar cosas perdidas
    val managerName: String = "",
    val managerPhone: String = "",
    val lostAndFoundNote: String? = null
)

// --- Respuesta del Panel de administrador ---
@Serializable
data class AdminPanelResponse(
    val title: String = "Panel de administrador",
    val establishment: Establishment
)

// --- Rama "Objetos perdidos" (pública para el cliente) ---
@Serializable
data class LostAndFoundInfo(
    val establishmentName: String,
    val managerName: String,
    val managerPhone: String,
    val note: String? = null,
    val message: String = "Llama al gerente para checar objetos perdidos."
)
