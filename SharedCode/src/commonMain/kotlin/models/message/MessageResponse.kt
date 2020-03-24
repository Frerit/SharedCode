package models.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val response : String,
    val message: String
)