package data

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import types.ReturnType

val surrealJson = Json { ignoreUnknownKeys = true }

val client = HttpClient(CIO){
    install(ContentNegotiation){
        json(surrealJson)
    }
    install(WebSockets)
}


