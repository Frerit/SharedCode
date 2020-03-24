package models.security

import kotlinx.serialization.Serializable

@Serializable
data class EnableLoginModel (
        val vssCookie : String?,
        val error : Boolean = false,
        val successCode : String?
)