package models

import kotlinx.serialization.*

@Serializable
data class ValidateUserDiscountParameter (
        val data: String
)