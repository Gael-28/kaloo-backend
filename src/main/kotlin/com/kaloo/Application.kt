package com.kaloo

import com.kaloo.config.AppConfig
import com.kaloo.config.configureDatabase
import com.kaloo.plugins.configureHttp
import com.kaloo.plugins.configureSerialization
import com.kaloo.routes.configureAdminRoutes
import com.kaloo.routes.configureRouting
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

/** Módulo principal usado por application.yaml y por testApplication. */
fun Application.module() {
    configureDatabase()
    configureHttp()
    configureSerialization()
    configureRouting()
    configureAdminRoutes()
}

/** Punto de entrada local (alternativa a io.ktor.server.cio.EngineMain). */
fun main() {
    embeddedServer(CIO, port = AppConfig.serverPort, host = AppConfig.serverHost) {
        module()
    }.start(wait = true)
}
