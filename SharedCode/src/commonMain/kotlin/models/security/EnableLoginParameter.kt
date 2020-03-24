package models.security

import kotlinx.serialization.Serializable

@Serializable
data class EnableLoginParameter(
        val vss: String,
        val email: String,
        val accesskey: String
)