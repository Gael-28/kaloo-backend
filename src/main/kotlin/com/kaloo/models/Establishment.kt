package com.kaloo.models

import kotlinx.serialization.Serializable

// --- Modelo del establecimiento ---
// En este apartado se visualizarA y gestionarA la informaciOn del establecimiento
@Serializable
data class Establishment(
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
    val openingHours: String,
    val description: String? = null,
    // Rama "Objetos perdidos": telEfono del gerente para checar cosas perdidas
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

// --- Rama "Objetos perdidos" (pUblica para el cliente) ---
@Serializable
data class LostItem(
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val color: String? = null,
    val imageBase64: String? = null, // imagen en base64 (ej. pixelada B/N)
    val dateFound: String? = null,
    val status: String = "ENCONTRADO" // ENCONTRADO / ENTREGADO
)

@Serializable
data class LostItemRequest(
    val name: String,
    val description: String = "",
    val color: String? = null,
    val imageBase64: String? = null
)

@Serializable
data class LostAndFoundInfo(
    val establishmentName: String,
    val managerName: String,
    val managerPhone: String,
    val note: String? = null,
    val message: String = "Llama al gerente para checar objetos perdidos.",
    val items: List<LostItem> = emptyList()
)
