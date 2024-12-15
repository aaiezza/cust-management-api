package io.github.aaiezza.custman.customer.data

import assertk.assertThat
import assertk.assertions.*
import io.github.aaiezza.custman.customer.CustomerAlreadyExistsWithGivenEmailException
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.sample
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test") // Load application-test.properties
class CreateCustomerStatementIntegrationTest(
    @Autowired private val resetDatabaseStatement: ResetDatabaseStatement,
    @Autowired private val subject: CreateCustomerStatement,
    @Autowired private val getCustomerByIdStatement: GetCustomerByIdStatement
) {
    @BeforeEach
    fun setUp() {
        resetDatabaseStatement.execute()
    }


    @Test
    fun `should create a new customer successfully`() {
        // Arrange
        val request = CreateCustomerRequest.sample

        // Act
        val customer = subject.execute(request)

        // Assert
        assertThat(customer).isNotNull()
        assertThat(customer.emailAddress).isEqualTo(request.emailAddress)
        assertThat(customer.fullName).isEqualTo(request.fullName)

        // Verify in database using GetCustomerByIdStatement
        val retrievedCustomer = getCustomerByIdStatement.execute(customer.customerId)
        assertThat(retrievedCustomer).isNotNull()
        assertThat(retrievedCustomer?.emailAddress).isEqualTo(request.emailAddress)
        assertThat(retrievedCustomer?.fullName).isEqualTo(request.fullName)
    }

    @Test
    fun `should throw exception when email already exists`() {
        // Arrange: Insert an existing customer
        subject.execute(CreateCustomerRequest.sample)

        // Act & Assert
        assertThat { subject.execute(CreateCustomerRequest.sample) }.isFailure()
            .isInstanceOf(CustomerAlreadyExistsWithGivenEmailException::class)
            .hasMessage("A customer with email `johnny+company@gmail.com` already exists")
    }
}
