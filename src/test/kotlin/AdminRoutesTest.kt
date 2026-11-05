package com.kaloo

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlin.test.*

class AdminRoutesTest {

    @Test
    fun `test panel de administrador`() = testApplication {
        configure()
        val response = client.get("/api/admin/panel")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Panel de administrador"))
    }

    @Test
    fun `test ver informacion del establecimiento`() = testApplication {
        configure()
        val response = client.get("/api/admin/establishment")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Kaloo Gym"))
    }
}
