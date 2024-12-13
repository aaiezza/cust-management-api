package io.github.aaiezza.custman.customer.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CustomerSerializationTest {

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        objectMapper =
            ObjectMapper().registerModule(JavaTimeModule())
    }

    @Test
    fun `serialize customer to json`() {
        val customer = Customer.sample

        val json = objectMapper.writeValueAsString(customer)

        val expected = """
            {
              "customer_id": "%s",
              "fullName": "%s",
              "preferredName": "%s",
              "email": "%s",
              "phoneNumber": "%s"
            }
            """.trimIndent()
            .replace(Regex("\\s+"), "")
            .format(
                customer.customerId.value,
                customer.fullName.value,
                customer.preferredName.value,
                customer.emailAddress.value,
                customer.phoneNumber.value,
            )

        assertThat(json).isEqualTo(expected)
    }
}
