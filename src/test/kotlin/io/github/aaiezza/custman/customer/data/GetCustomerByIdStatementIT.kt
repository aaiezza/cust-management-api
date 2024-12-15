package io.github.aaiezza.custman.customer.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.sample
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest
@ActiveProfiles("test") // Load application-test.properties
class GetCustomerByIdStatementIT(
    @Autowired private val resetDatabaseStatement: ResetDatabaseStatement,
    @Autowired private val createCustomerStatement: CreateCustomerStatement,
    @Autowired private val subject: GetCustomerByIdStatement
) {
    @BeforeEach
    fun setUp() {
        resetDatabaseStatement.execute()
    }

    @Test
    fun `should retrieve an existing customer by ID`() {
        // Arrange
        val request = CreateCustomerRequest.sample.copy(
            emailAddress = Customer.EmailAddress("unique-email@example.com")
        )
        val createdCustomer = createCustomerStatement.execute(request)

        // Act
        val retrievedCustomer = subject.execute(createdCustomer.customerId)

        // Assert
        assertThat(retrievedCustomer).isNotNull()
        assertThat(retrievedCustomer?.customerId).isEqualTo(createdCustomer.customerId)
        assertThat(retrievedCustomer?.emailAddress).isEqualTo(createdCustomer.emailAddress)
        assertThat(retrievedCustomer?.fullName).isEqualTo(createdCustomer.fullName)
        assertThat(retrievedCustomer?.createdAt).isEqualTo(createdCustomer.createdAt)
        assertThat(retrievedCustomer?.updatedAt).isEqualTo(createdCustomer.updatedAt)
    }

    @Test
    fun `should return null for a non-existent customer ID`() {
        // Arrange
        val nonExistentCustomerId = Customer.Id(UUID.fromString("000a0000-0000-0000-0000-00070000000"))

        // Act
        val retrievedCustomer = subject.execute(nonExistentCustomerId)

        // Assert
        assertThat(retrievedCustomer).isNull()
    }
}

