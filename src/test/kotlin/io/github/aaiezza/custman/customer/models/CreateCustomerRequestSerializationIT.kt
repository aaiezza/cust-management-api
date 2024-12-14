package io.github.aaiezza.custman.customer.models

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest

@JsonTest
class CreateCustomerRequestSerializationIT {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should serialize sample CreateCustomerRequest to JSON`() {
        val createRequest = CreateCustomerRequest.sample

        val expectedJson = """
            {
                "full_name": "${createRequest.fullName.value}",
                "preferred_name": "${createRequest.preferredName.value}",
                "email_address": "${createRequest.emailAddress.value}",
                "phone_number": "${createRequest.phoneNumber.value}"
            }
        """.trimIndent()

        // Serialize the sample object
        val serializedJson = objectMapper.writeValueAsString(createRequest)

        // Compare JSON structures
        val expectedNode = objectMapper.readTree(expectedJson)
        val actualNode = objectMapper.readTree(serializedJson)

        assertEquals(expectedNode, actualNode)
    }

    @Test
    fun `should deserialize JSON to CreateCustomerRequest`() {
        val createRequest = CreateCustomerRequest.sample
        val json = """
            {
                "full_name": "${createRequest.fullName.value}",
                "preferred_name": "${createRequest.preferredName.value}",
                "email_address": "${createRequest.emailAddress.value}",
                "phone_number": "${createRequest.phoneNumber.value}"
            }
        """.trimIndent()

        // Deserialize JSON to object
        val deserializedRequest = objectMapper.readValue(json, CreateCustomerRequest::class.java)

        assertEquals(createRequest, deserializedRequest)
    }
}
