package io.github.aaiezza.custman.customer.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

class CustomerSerializationTest {

    private val objectMapper = ObjectMapper()

    @Test
    fun `serialize customer to json`() {
        val customer = Customer.Companion.sample

        val json = objectMapper.writeValueAsString(customer)

        val expected = """
            {
              "id": "%s",
              "fullName": "%s",
              "preferredName": "%s",
              "email": "%s",
              "phoneNumber": "%s"
            }
            """.trimIndent()
            .replace(Regex("\\s+"), "")
            .format(
                customer.id.value,
                customer.fullName.value,
                customer.preferredName.value,
                customer.email.value,
                customer.phoneNumber.value,
            )

        assertThat(json).isEqualTo(expected)
    }
}
