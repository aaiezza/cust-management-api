package io.github.aaiezza.custman.customer.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.regex.Pattern

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Customer(
    @JsonProperty("customer_id") val customerId: Id,
    @JsonProperty("full_name") val fullName: FullName,
    @JsonProperty("preferred_name") val preferredName: PreferredName,
    @JsonProperty("email_address") val emailAddress: EmailAddress,
    @JsonProperty("phone_number") val phoneNumber: PhoneNumber,
    @JsonProperty("created_at") val createdAt: CreatedAt,
    @JsonProperty("updated_at") val updatedAt: UpdatedAt,
) {
    data class Id(@JsonValue val value: UUID) {
        override fun toString() = value.toString()
    }

    data class FullName(@JsonValue val value: String) {
        init {
            require(value.isNotBlank()) { "Full name cannot be blank." }
        }
    }

    data class PreferredName(@JsonValue val value: String) {
        init {
            require(value.isNotBlank()) { "Preferred name cannot be blank." }
        }
    }

    data class EmailAddress(@JsonValue val value: String) {
        init {
            require(value.isNotBlank()) { "Email address cannot be blank." }
            require(EMAIL_REGEX.matcher(value).matches()) { "Invalid email address format." }
        }

        companion object {
            private val EMAIL_REGEX = Pattern.compile(
                "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"
            )
        }
    }

    data class PhoneNumber(@JsonValue val value: String) {
        init {
            require(value.isNotBlank()) { "Phone number cannot be blank." }
            require(PHONE_NUMBER_REGEX.matcher(value).matches()) {
                "Phone number must conform to the E.164 format."
            }
        }

        companion object {
            private val PHONE_NUMBER_REGEX = Pattern.compile(
                "^\\+?[1-9]\\d{1,14}$"
            )
        }
    }

    data class CreatedAt(@JsonValue val value: OffsetDateTime)
    data class UpdatedAt(@JsonValue val value: OffsetDateTime)

    data class Stub(
        val id: Id,
        val fullName: FullName,
        val preferredName: PreferredName,
        val emailAddress: EmailAddress,
        val phoneNumber: PhoneNumber,
    )

    companion object
}

val Customer.Companion.sample
    get() = Customer(
        customerId = Customer.Id(UUID.fromString("00001111-2222-3333-aaaa-bbbbccccdddd")),
        fullName = Customer.FullName("John Doe III"),
        preferredName = Customer.PreferredName("Johnny"),
        emailAddress = Customer.EmailAddress("johnny+company@gmail.com"),
        phoneNumber = Customer.PhoneNumber("+12223334444"),
        createdAt = Customer.CreatedAt(OffsetDateTime.of(2017, 8, 4, 0, 0, 0, 0, ZoneOffset.UTC)),
        updatedAt = Customer.UpdatedAt(OffsetDateTime.of(2017, 8, 4, 0, 0, 0, 0, ZoneOffset.UTC)),
    )

