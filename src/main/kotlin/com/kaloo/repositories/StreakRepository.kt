package com.kaloo.repositories

import com.kaloo.models.UserStreak
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * Repositorio en memoria de rachas.
 * Cuando exista SQL Server, reemplazar por tabla UserStreaks.
 */
object StreakRepository {

    private val streaks = ConcurrentHashMap<String, UserStreak>()

    fun get(username: String): UserStreak =
        streaks[username] ?: UserStreak(username = username)

    /**
     * Registra asistencia de hoy:
     * - primer check-in        → racha = 1
     * - ayer también entrenó   → racha + 1
     * - ya entrenó hoy         → sin cambios
     * - saltó más de 1 día     → racha = 1 (se reinicia)
     */
    fun checkIn(username: String, today: LocalDate = LocalDate.now()): UserStreak {
        val current = get(username)
        val last = current.lastCheckIn?.let { LocalDate.parse(it) }
        val todayStr = today.toString()

        val newStreak = when {
            last == null -> 1
            last == today -> current.currentStreak // ya registró hoy
            last == today.minusDays(1) -> current.currentStreak + 1
            else -> 1 // rompió la racha
        }

        val updated = current.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(current.longestStreak, newStreak),
            totalCheckIns = if (last == today) current.totalCheckIns else current.totalCheckIns + 1,
            lastCheckIn = todayStr,
            message = when {
                last == today -> "Ya registraste tu asistencia de hoy."
                newStreak > 1 -> "¡Racha de $newStreak días! Sigue así."
                else -> "Primer día de tu racha. ¡Vamos!"
            }
        )
        streaks[username] = updated
        return updated
    }
}
