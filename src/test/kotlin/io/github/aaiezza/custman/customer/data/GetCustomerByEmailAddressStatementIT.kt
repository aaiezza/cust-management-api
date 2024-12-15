package io.github.aaiezza.custman.customer.data

import assertk.assertThat
import assertk.assertions.*
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.sample
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GetCustomerByEmailAddressStatementIT(
    @Autowired private val resetDatabaseStatement: ResetDatabaseStatement,
    @Autowired private val createCustomerStatement: CreateCustomerStatement,
    @Autowired private val softDeleteCustomerStatement: SoftDeleteCustomerStatement,
    @Autowired private val subject: GetCustomerByEmailAddressStatement,
) {

    @BeforeEach
    fun setUp() {
        resetDatabaseStatement.execute()
    }

    @Test
    fun `should retrieve an existing customer by email`() {
        // Arrange
        val request = CreateCustomerRequest.sample.copy(
            emailAddress = Customer.EmailAddress("exists@example.com")
        )
        val createdCustomer = createCustomerStatement.execute(request)

        // Act
        val retrievedCustomer = subject.execute(request.emailAddress)

        // Assert
        assertThat(retrievedCustomer).isNotNull()
        assertThat(retrievedCustomer?.customerId).isEqualTo(createdCustomer.customerId)
        assertThat(retrievedCustomer?.emailAddress).isEqualTo(createdCustomer.emailAddress)
        assertThat(retrievedCustomer?.fullName).isEqualTo(createdCustomer.fullName)
        assertThat(retrievedCustomer?.phoneNumber).isEqualTo(createdCustomer.phoneNumber)
    }

    @Test
    fun `should return null for a non-existent email`() {
        // Arrange
        val nonExistentEmail = Customer.EmailAddress("nonexistent@example.com")

        // Act
        val retrievedCustomer = subject.execute(nonExistentEmail)

        // Assert
        assertThat(retrievedCustomer).isNull()
    }

    @Test
    fun `should return null for a soft-deleted customer`() {
        // Arrange
        val request = CreateCustomerRequest.sample.copy(
            emailAddress = Customer.EmailAddress("softdeleted@example.com")
        )
        val createdCustomer = createCustomerStatement.execute(request)

        // Simulate soft-delete
        assertThat { softDeleteCustomerStatement.execute(createdCustomer.customerId) }.isSuccess().isTrue()

        // Act
        val retrievedCustomer = subject.execute(request.emailAddress)

        // Assert
        assertThat(retrievedCustomer).isNull()
    }
}
