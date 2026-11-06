package com.kaloo

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.HttpTimeout

// --- Modelos para la REST API de Google Gemini ---

@Serializable
data class GeminiPart(val text: String)

@Serializable
data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
data class GeminiRequest(val contents: List<GeminiContent>)

@Serializable
data class GeminiCandidate(val content: GeminiContent? = null)

@Serializable
data class GeminiErrorDetails(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiErrorDetails? = null
)

// --- Servicio HTTP ---

class GeminiService(private val apiKey: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }


        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }
    }

    suspend fun generateContent(prompt: String): String {
        if (apiKey.isBlank()) {
            return "Error: Configura tu API Key de Gemini en application.yaml"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$apiKey"

        return try {
            val httpResponse: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(listOf(GeminiContent(listOf(GeminiPart(prompt))))))
            }

            val rawBody = httpResponse.bodyAsText()

            if (!httpResponse.status.isSuccess()) {
                val errorParsed = try {
                    Json { ignoreUnknownKeys = true }.decodeFromString<GeminiResponse>(rawBody)
                } catch (e: Exception) { null }

                val message = errorParsed?.error?.message ?: rawBody
                return "Error HTTP ${httpResponse.status.value}: $message"
            }

            val response = Json { ignoreUnknownKeys = true }.decodeFromString<GeminiResponse>(rawBody)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "La IA respondió pero no generó texto."
        } catch (e: Exception) {
            "Error de conexión con Gemini: ${e.message}"
        }
    }
}