package io.github.aaiezza.kutils

import com.fasterxml.jackson.annotation.JsonValue
import java.util.*

/* Domain Specific Identifier */
abstract class Dsi(private val namespace: String, private val name: String, open val uuid: UUID) {
    init {
        require(value.startsWith("$namespace:$name:")) { "Invalid ID format: $value" }
    }

    @get:JsonValue
    val value: String
        get() = "$namespace:$name:$uuid"

    companion object {
        fun toUUID(value: String): UUID = value.substringAfterLast(":").let(UUID::fromString)
    }
}
