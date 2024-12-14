package io.github.aaiezza.custman.customer.models

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest

@JsonTest
class CustomerSerializationIT {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should serialize sample Customer object to JSON`() {
        val customer = Customer.sample

        val expectedJson = """
            {
                "customer_id": "${customer.customerId.value}",
                "full_name": "${customer.fullName.value}",
                "preferred_name": "${customer.preferredName.value}",
                "email_address": "${customer.emailAddress.value}",
                "phone_number": "${customer.phoneNumber.value}",
                "created_at": "${customer.createdAt.value}",
                "updated_at": "${customer.updatedAt.value}"
            }
        """.trimIndent()

        val serializedJson = objectMapper.writeValueAsString(customer)

        val expectedNode = objectMapper.readTree(expectedJson)
        val actualNode = objectMapper.readTree(serializedJson)

        assertEquals(expectedNode, actualNode)
    }

    @Test
    fun `should deserialize JSON to Customer object`() {
        val customer = Customer.sample
        val json = """
            {
                "customer_id": "${customer.customerId.value}",
                "full_name": "${customer.fullName.value}",
                "preferred_name": "${customer.preferredName.value}",
                "email_address": "${customer.emailAddress.value}",
                "phone_number": "${customer.phoneNumber.value}",
                "created_at": "${customer.createdAt.value}",
                "updated_at": "${customer.updatedAt.value}"
            }
        """.trimIndent()

        val deserializedCustomer = objectMapper.readValue(json, Customer::class.java)

        assertEquals(customer, deserializedCustomer)
    }
}
