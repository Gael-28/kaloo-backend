package com.kaloo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

// Cliente de IA compartido. Lo pueden usar la recomendacion de ejercicios
// y el plan nutricional: cada quien manda su propio prompt a consultarIA().
//
// La llave NUNCA va en la app ni en el codigo. Se lee de una variable de entorno:
//   Windows (CMD):  set ANTHROPIC_API_KEY=tu_llave   y luego   gradlew run
// Si no existe la variable, consultarIA() devuelve null y cada modulo usa su respaldo.

private val clienteHttp: HttpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build()

private val jsonIA = Json { ignoreUnknownKeys = true }

fun iaDisponible(): Boolean = !System.getenv("ANTHROPIC_API_KEY").isNullOrBlank()

/** Devuelve el texto de la respuesta de la IA, o null si no hay llave o algo fallo. */
suspend fun consultarIA(instrucciones: String, mensaje: String, maxTokens: Int = 1500): String? {
    val llave = System.getenv("ANTHROPIC_API_KEY")?.trim()
    if (llave.isNullOrEmpty()) return null
    val modeloEnv = System.getenv("KALOO_IA_MODELO")?.trim()
    val modelo = if (modeloEnv.isNullOrEmpty()) "claude-haiku-5-5" else modeloEnv

    val cuerpo = buildJsonObject {
        put("model", modelo)
        put("max_tokens", maxTokens)
        put("system", instrucciones)
        putJsonArray("messages") {
            addJsonObject {
                put("role", "user")
                put("content", mensaje)
            }
        }
    }.toString()

    return withContext(Dispatchers.IO) {
        try {
            val peticion = HttpRequest.newBuilder(URI.create("https://api.anthropic.com/v1/messages"))
                .timeout(Duration.ofSeconds(45))
                .header("content-type", "application/json")
                .header("x-api-key", llave)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build()
            val respuesta = clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofString())
            if (respuesta.statusCode() != 200) {
                println("IA: respondio ${respuesta.statusCode()} ${respuesta.body().take(200)}")
                null
            } else {
                val raiz = jsonIA.parseToJsonElement(respuesta.body()).jsonObject
                raiz["content"]?.jsonArray
                    ?.map { it.jsonObject }
                    ?.firstOrNull { it["type"]?.jsonPrimitive?.content == "text" }
                    ?.get("text")?.jsonPrimitive?.content
            }
        } catch (e: Exception) {
            println("IA: fallo la consulta (${e.message})")
            null
        }
    }
}