package io.github.aaiezza.custman.customer.data

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.sample
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test") // Load application-test.properties
class EmailExistsStatementIT(
    @Autowired private val resetDatabaseStatement: ResetDatabaseStatement,
    @Autowired private val createCustomerStatement: CreateCustomerStatement,
    @Autowired private val softDeleteCustomerStatement: SoftDeleteCustomerStatement,
    @Autowired private val subject: EmailExistsStatement
) {

    @BeforeEach
    fun setUp() {
        resetDatabaseStatement.execute()
    }

    @Test
    fun `should return true when email exists and is not soft-deleted`() {
        // Arrange
        val request = CreateCustomerRequest.sample.copy(
            emailAddress = Customer.EmailAddress("exists@example.com")
        )
        createCustomerStatement.execute(request)

        // Act
        val emailExists = subject.execute(request.emailAddress)

        // Assert
        assertThat(emailExists).isTrue()
    }

    @Test
    fun `should return false when email does not exist`() {
        // Arrange
        val nonExistentEmail = Customer.EmailAddress("nonexistent@example.com")

        // Act
        val emailExists = subject.execute(nonExistentEmail)

        // Assert
        assertThat(emailExists).isFalse()
    }

    @Test
    fun `should return false when email is soft-deleted`() {
        // Arrange
        val request = CreateCustomerRequest.sample.copy(
            emailAddress = Customer.EmailAddress("softdeleted@example.com")
        )
        val createdCustomer = createCustomerStatement.execute(request)

        softDeleteCustomerStatement.execute(createdCustomer.customerId)

        // Act
        val emailExists = subject.execute(request.emailAddress)

        // Assert
        assertThat(emailExists).isFalse()
    }
}
