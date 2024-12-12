package io.github.aaiezza.custman.customer.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.util.*
import java.util.regex.Pattern

data class Customer(
    @JsonProperty("id") val id: Id,
    @JsonProperty("full_name") val fullName: FullName,
    @JsonProperty("preferred_name") val preferredName: PreferredName,
    @JsonProperty("email_address") val email: Email,
    @JsonProperty("phone_number") val phoneNumber: PhoneNumber,
) {
    data class Id(@JsonValue val value: UUID)

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

    data class Email(@JsonValue val value: String) {
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

    companion object {}
}

val Customer.Companion.sample
    get() = Customer(
        id = Customer.Id(UUID.fromString("00001111-2222-3333-aaaa-bbbbccccdddd")),
        fullName = Customer.FullName("John Doe III"),
        preferredName = Customer.PreferredName("Johnny"),
        email = Customer.Email("johnny+company@gmail.com"),
        phoneNumber = Customer.PhoneNumber("+12223334444"),
    )

