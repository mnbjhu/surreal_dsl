package core

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val code: Int, val token: String, val details: String)
