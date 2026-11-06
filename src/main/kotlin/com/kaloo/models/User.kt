package com.kaloo.models

import kotlinx.serialization.Serializable

/** Usuario persistido (por ahora desde users.json, luego desde SQL Server). */
@Serializable
data class User(val username: String, val password: String, val role: String)
