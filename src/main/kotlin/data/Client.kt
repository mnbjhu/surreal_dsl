package data

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val surrealJson = Json { ignoreUnknownKeys = true }

val client = HttpClient(CIO){
    install(ContentNegotiation){
        json()
    }
}
