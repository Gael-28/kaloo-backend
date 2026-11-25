package com.kaloo.repositories

import com.kaloo.config.Database
import com.kaloo.models.UserStreak
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * Repositorio de rachas. Usa PostgreSQL (Railway) con fallback en memoria.
 */
object StreakRepository {

    private val memory = ConcurrentHashMap<String, UserStreak>()

    fun get(username: String): UserStreak = runCatching {
        Database.connect().use { conn ->
            conn.prepareStatement(
                "SELECT username, current_streak, longest_streak, total_check_ins, last_check_in FROM user_streaks WHERE username = ?"
            ).use { ps ->
                ps.setString(1, username)
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.toStreak() else UserStreak(username = username).also { memory[username] = it }
                }
            }
        }
    }.getOrElse { memory[username] ?: UserStreak(username = username) }

    fun checkIn(username: String, today: LocalDate = LocalDate.now()): UserStreak = runCatching {
        Database.connect().use { conn ->
            conn.autoCommit = false
            try {
                val current = conn.prepareStatement(
                    "SELECT username, current_streak, longest_streak, total_check_ins, last_check_in FROM user_streaks WHERE username = ? FOR UPDATE"
                ).use { ps ->
                    ps.setString(1, username)
                    ps.executeQuery().use { rs -> if (rs.next()) rs.toStreak() else UserStreak(username = username) }
                }

                val last = current.lastCheckIn?.let { LocalDate.parse(it) }
                val todayStr = today.toString()
                val newStreak = when {
                    last == null -> 1
                    last == today -> current.currentStreak
                    last == today.minusDays(1) -> current.currentStreak + 1
                    else -> 1
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

                conn.prepareStatement(
                    """
                    INSERT INTO user_streaks (username, current_streak, longest_streak, total_check_ins, last_check_in)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT (username) DO UPDATE SET
                        current_streak = EXCLUDED.current_streak,
                        longest_streak = EXCLUDED.longest_streak,
                        total_check_ins = EXCLUDED.total_check_ins,
                        last_check_in = EXCLUDED.last_check_in
                    """.trimIndent()
                ).use { ps ->
                    ps.setString(1, updated.username)
                    ps.setInt(2, updated.currentStreak)
                    ps.setInt(3, updated.longestStreak)
                    ps.setInt(4, updated.totalCheckIns)
                    ps.setDate(5, java.sql.Date.valueOf(updated.lastCheckIn))
                    ps.executeUpdate()
                }

                conn.commit()
                memory[username] = updated
                updated
            } catch (e: Exception) {
                conn.rollback()
                throw e
            } finally {
                conn.autoCommit = true
            }
        }
    }.getOrElse {
        val updated = checkInMemory(username, today)
        memory[username] = updated
        updated
    }

    private fun checkInMemory(username: String, today: LocalDate): UserStreak {
        val current = memory[username] ?: UserStreak(username = username)
        val last = current.lastCheckIn?.let { LocalDate.parse(it) }
        val todayStr = today.toString()
        val newStreak = when {
            last == null -> 1
            last == today -> current.currentStreak
            last == today.minusDays(1) -> current.currentStreak + 1
            else -> 1
        }
        return current.copy(
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
    }

    private fun java.sql.ResultSet.toStreak(): UserStreak = UserStreak(
        username = getString("username"),
        currentStreak = getInt("current_streak"),
        longestStreak = getInt("longest_streak"),
        totalCheckIns = getInt("total_check_ins"),
        lastCheckIn = getDate("last_check_in")?.toLocalDate()?.toString()
    )
}
