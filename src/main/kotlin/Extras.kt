package com.kaloo

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

// ---------- Modelos: racha ----------
@Serializable
data class StreakCheckInRequest(val username: String)

@Serializable
data class StreakDay(val label: String, val date: String, val done: Boolean)

@Serializable
data class StreakResponse(
    val username: String,
    val current: Int,
    val best: Int,
    val totalDays: Int,
    val checkedInToday: Boolean,
    val firstCheckInToday: Boolean,
    val week: List<StreakDay>
)

// ---------- Modelos: objetos perdidos ----------
@Serializable
data class LostItem(
    val id: String,
    val title: String,
    val description: String = "",
    val reporter: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoBase64: String? = null,
    val createdAt: String,
    val resolved: Boolean = false
)

@Serializable
data class LostItemRequest(
    val title: String,
    val description: String = "",
    val reporter: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoBase64: String? = null
)

// ---------- Almacenamiento en archivos JSON (carpeta ./data) ----------
private val zone: ZoneId = ZoneId.of("America/Mexico_City")
private val storeJson = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
private val lock = Any()
private val dataDir = File("data").apply { mkdirs() }
private val streakFile = File(dataDir, "streaks.json")
private val itemsFile = File(dataDir, "lost_items.json")

private val streakSerializer = MapSerializer(String.serializer(), ListSerializer(String.serializer()))
private val itemsSerializer = ListSerializer(LostItem.serializer())

private val checkIns: MutableMap<String, MutableSet<String>> by lazy {
    val map = mutableMapOf<String, MutableSet<String>>()
    if (streakFile.exists()) {
        runCatching {
            storeJson.decodeFromString(streakSerializer, streakFile.readText())
                .forEach { (user, days) -> map[user] = days.toMutableSet() }
        }
    }
    map
}

private val lostItems: MutableList<LostItem> by lazy {
    if (itemsFile.exists()) {
        runCatching { storeJson.decodeFromString(itemsSerializer, itemsFile.readText()).toMutableList() }
            .getOrElse { mutableListOf() }
    } else mutableListOf()
}

private fun saveStreaks() {
    streakFile.writeText(storeJson.encodeToString(streakSerializer, checkIns.mapValues { it.value.sorted() }))
}

private fun saveItems() {
    itemsFile.writeText(storeJson.encodeToString(itemsSerializer, lostItems))
}

// ---------- Lógica de racha ----------
private const val DAY_LABELS = "LMXJVSD" // lunes..domingo

private fun buildStreak(username: String, firstToday: Boolean): StreakResponse {
    val days = checkIns[username].orEmpty().map { LocalDate.parse(it) }.toSortedSet()
    val today = LocalDate.now(zone)

    // racha actual: días consecutivos que terminan hoy (o ayer, si hoy aún no ha entrado)
    var cursor = if (today in days) today else today.minusDays(1)
    var current = 0
    while (cursor in days) {
        current++
        cursor = cursor.minusDays(1)
    }

    // mejor racha histórica
    var best = 0
    var run = 0
    var prev: LocalDate? = null
    for (d in days) {
        run = if (prev != null && prev.plusDays(1) == d) run + 1 else 1
        if (run > best) best = run
        prev = d
    }

    val week = (6 downTo 0).map { back ->
        val d = today.minusDays(back.toLong())
        StreakDay(
            label = DAY_LABELS[d.dayOfWeek.value - 1].toString(),
            date = d.toString(),
            done = d in days
        )
    }

    return StreakResponse(
        username = username,
        current = current,
        best = best,
        totalDays = days.size,
        checkedInToday = today in days,
        firstCheckInToday = firstToday,
        week = week
    )
}

// ---------- Rutas ----------
fun Application.configureExtras() {
    routing {

        // Registra la asistencia de hoy (se llama al iniciar sesión). Es idempotente.
        post("/api/streak/checkin") {
            val req = call.receive<StreakCheckInRequest>()
            if (req.username.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Usuario requerido"))
                return@post
            }
            val response = synchronized(lock) {
                val today = LocalDate.now(zone).toString()
                val set = checkIns.getOrPut(req.username) { mutableSetOf() }
                val isNew = set.add(today)
                if (isNew) saveStreaks()
                buildStreak(req.username, isNew)
            }
            call.respond(response)
        }

        get("/api/streak/{username}") {
            val username = call.parameters["username"].orEmpty()
            val response = synchronized(lock) { buildStreak(username, false) }
            call.respond(response)
        }

        // Lista de objetos perdidos (los más recientes primero)
        get("/api/lost-items") {
            val list = synchronized(lock) { lostItems.sortedByDescending { it.createdAt } }
            call.respond(list)
        }

        post("/api/lost-items") {
            val req = call.receive<LostItemRequest>()
            if (req.title.isBlank() || req.reporter.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Título y usuario son requeridos"))
                return@post
            }
            if ((req.photoBase64?.length ?: 0) > 6_000_000) {
                call.respond(HttpStatusCode.PayloadTooLarge, ApiResponse(false, "La foto es demasiado grande"))
                return@post
            }
            val item = LostItem(
                id = UUID.randomUUID().toString(),
                title = req.title.trim(),
                description = req.description.trim(),
                reporter = req.reporter,
                latitude = req.latitude,
                longitude = req.longitude,
                photoBase64 = req.photoBase64,
                createdAt = java.time.OffsetDateTime.now(zone).toString()
            )
            synchronized(lock) {
                lostItems.add(item)
                saveItems()
            }
            call.respond(item)
        }

        // Marcar como encontrado / entregado
        post("/api/lost-items/{id}/resolve") {
            val id = call.parameters["id"].orEmpty()
            val ok = synchronized(lock) {
                val idx = lostItems.indexOfFirst { it.id == id }
                if (idx >= 0) {
                    lostItems[idx] = lostItems[idx].copy(resolved = true)
                    saveItems()
                    true
                } else false
            }
            if (ok) call.respond(ApiResponse(true, "Marcado como resuelto"))
            else call.respond(HttpStatusCode.NotFound, ApiResponse(false, "No existe ese objeto"))
        }
    }
}