package io.github.aaiezza.custman.customer.models

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest

@JsonTest
class UpdateCustomerRequestSerializationIT {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should serialize UpdateCustomerRequest to JSON`() {
        // Arrange
        val updateRequest = UpdateCustomerRequest.sample

        val expectedJson = """
            {
                "full_name": "${updateRequest.fullName.value}",
                "preferred_name": "${updateRequest.preferredName.value}",
                "email_address": "${updateRequest.emailAddress.value}",
                "phone_number": "${updateRequest.phoneNumber.value}"
            }
        """.trimIndent()

        // Act
        val serializedJson = objectMapper.writeValueAsString(updateRequest)

        // Assert
        val expectedNode = objectMapper.readTree(expectedJson)
        val actualNode = objectMapper.readTree(serializedJson)
        assertEquals(expectedNode, actualNode)
    }

    @Test
    fun `should deserialize JSON to UpdateCustomerRequest`() {
        // Arrange
        val updateRequest = UpdateCustomerRequest.sample

        val json = """
            {
                "full_name": "${updateRequest.fullName.value}",
                "preferred_name": "${updateRequest.preferredName.value}",
                "email_address": "${updateRequest.emailAddress.value}",
                "phone_number": "${updateRequest.phoneNumber.value}"
            }
        """.trimIndent()

        // Act
        val deserializedRequest = objectMapper.readValue(json, UpdateCustomerRequest::class.java)

        // Assert
        assertEquals(updateRequest, deserializedRequest)
    }
}
