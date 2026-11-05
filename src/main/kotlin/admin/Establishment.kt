package com.kaloo.admin

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
    val description: String? = null
)

// --- Respuesta del Panel de administrador ---
@Serializable
data class AdminPanelResponse(
    val title: String = "Panel de administrador",
    val establishment: Establishment
)
