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
class GetAllCustomersStatementIT(
    @Autowired private val resetDatabaseStatement: ResetDatabaseStatement,
    @Autowired private val createCustomerStatement: CreateCustomerStatement,
    @Autowired private val softDeleteCustomerStatement: SoftDeleteCustomerStatement,
    @Autowired private val subject: GetAllCustomersStatement,
) {

    @BeforeEach
    fun setUp() {
        resetDatabaseStatement.execute()
    }

    @Test
    fun `should return all existing customers`() {
        // Arrange
        val customer1 = createCustomerStatement.execute(
            CreateCustomerRequest.sample.copy(
                emailAddress = Customer.EmailAddress("customer1@example.com")
            )
        )
        val customer2 = createCustomerStatement.execute(
            CreateCustomerRequest.sample.copy(
                emailAddress = Customer.EmailAddress("customer2@example.com")
            )
        )

        // Act
        val customers = subject.execute()

        // Assert
        assertThat(customers.value).hasSize(2)
        assertThat(customers.value[0].emailAddress).isEqualTo(customer2.emailAddress)
        assertThat(customers.value[1].emailAddress).isEqualTo(customer1.emailAddress)
    }

    @Test
    fun `should return an empty list when no customers exist`() {
        // Act
        val customers = subject.execute()

        // Assert
        assertThat(customers.value).isEmpty()
    }

    @Test
    fun `should exclude soft-deleted customers`() {
        // Arrange
        val customer1 = createCustomerStatement.execute(
            CreateCustomerRequest.sample.copy(
                emailAddress = Customer.EmailAddress("customer1@example.com")
            )
        )
        val customer2 = createCustomerStatement.execute(
            CreateCustomerRequest.sample.copy(
                emailAddress = Customer.EmailAddress("customer2@example.com")
            )
        )

        // Simulate soft-delete for customer1
        assertThat { softDeleteCustomerStatement.execute(customer1.customerId) }.isSuccess().isTrue()

        // Act
        val customers = subject.execute()

        // Assert
        assertThat(customers.value).hasSize(1)
        assertThat(customers.value[0].emailAddress).isEqualTo(customer2.emailAddress)
    }
}
