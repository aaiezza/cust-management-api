package io.github.aaiezza.custman.customer.data

import assertk.assertThat
import assertk.assertions.*
import io.github.aaiezza.custman.customer.CustomerNotFoundException
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.UpdateCustomerRequest
import io.github.aaiezza.custman.customer.models.sample
import io.github.aaiezza.custman.jooq.generated.Tables
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import org.jooq.impl.DefaultDSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UpdateCustomerStatementIT(
    @Autowired private val resetDatabaseStatement: ResetDatabaseStatement,
    @Autowired private val createCustomerStatement: CreateCustomerStatement,
    @Autowired private val getCustomerByIdStatement: GetCustomerByIdStatement,
    @Autowired private val softDeleteCustomerStatement: SoftDeleteCustomerStatement,
    @Autowired private val subject: UpdateCustomerStatement,
) {

    @Autowired
    private lateinit var dslContext: DefaultDSLContext

    @BeforeEach
    fun setUp() {
        resetDatabaseStatement.execute()
    }

    @Test
    fun `should update an existing customer`() {
        // Arrange
        val originalCustomer = createCustomerStatement.execute(
            CreateCustomerRequest.sample.copy(
                emailAddress = Customer.EmailAddress("update@example.com"),
                fullName = Customer.FullName("Original Name")
            )
        )

        val updateRequest = UpdateCustomerRequest(
            fullName = Customer.FullName("Updated Name"),
            preferredName = Customer.PreferredName("Updated Preferred Name"),
            emailAddress = Customer.EmailAddress("updated@example.com"),
            phoneNumber = Customer.PhoneNumber("+9876543210")
        )

        // Act
        subject.execute(originalCustomer.customerId, updateRequest)

        // Assert
        val updatedCustomer = getCustomerByIdStatement.execute(originalCustomer.customerId)
        assertThat(updatedCustomer).isNotNull()
        assertThat(updatedCustomer?.fullName).isEqualTo(updateRequest.fullName)
        assertThat(updatedCustomer?.preferredName).isEqualTo(updateRequest.preferredName)
        assertThat(updatedCustomer?.emailAddress).isEqualTo(updateRequest.emailAddress)
        assertThat(updatedCustomer?.phoneNumber).isEqualTo(updateRequest.phoneNumber)
    }

    @Test
    fun `should throw exception when updating a non-existent customer`() {
        // Arrange
        val nonExistentCustomerId = Customer.Id(UUID.fromString("00a00000-0000-0000-0000-00000070000"))
        val updateRequest = UpdateCustomerRequest(
            fullName = Customer.FullName("Non-existent Name"),
            preferredName = Customer.PreferredName("Non-existent Preferred Name"),
            emailAddress = Customer.EmailAddress("nonexistent@example.com"),
            phoneNumber = Customer.PhoneNumber("+1234567890")
        )

        // Act & Assert
        val exception = org.junit.jupiter.api.assertThrows<Exception> {
            subject.execute(nonExistentCustomerId, updateRequest)
        }
        assertThat(exception.message).isEqualTo("A customer with id `${nonExistentCustomerId.value}` was not found")
    }

    @Test
    fun `should create, update, delete, and fail to update the customer`() {
        // Arrange: Create a customer
        val originalCustomer = CreateCustomerRequest.sample.copy(
            emailAddress = Customer.EmailAddress("update-delete@example.com"),
            fullName = Customer.FullName("Original Name")
        ).let(createCustomerStatement::execute)

        // Act: Update the customer
        val updatedRequest = UpdateCustomerRequest(
            fullName = Customer.FullName("Updated Name"),
            preferredName = Customer.PreferredName("Updated Preferred Name"),
            emailAddress = Customer.EmailAddress("updated-email@example.com"),
            phoneNumber = Customer.PhoneNumber("+9876543210")
        )
        val updatedCustomer = subject.execute(originalCustomer.customerId, updatedRequest)

        // Assert: Ensure the customer was updated
        assertThat(updatedCustomer).isNotNull()
        assertThat(updatedCustomer.fullName).isEqualTo(updatedRequest.fullName)
        assertThat(updatedCustomer.preferredName).isEqualTo(updatedRequest.preferredName)
        assertThat(updatedCustomer.emailAddress).isEqualTo(updatedRequest.emailAddress)

        // Act: Soft delete the customer
        assertThat { softDeleteCustomerStatement.execute(originalCustomer.customerId) }.isSuccess().isTrue()

        // Assert: Ensure the customer is soft-deleted
        val softDeletedCustomer = getCustomerByIdStatement.execute(originalCustomer.customerId)
        assertThat(softDeletedCustomer).isNull()
        dslContext.selectFrom(CUSTOMER)
            .where(CUSTOMER.CUSTOMER_ID.eq(originalCustomer.customerId.value))
            .fetchOne(CUSTOMER.DELETED_AT)
            .let { assertThat(it).isNotNull() }

        // Act & Assert: Attempt to update the soft-deleted customer and assert failure
        val failedUpdateRequest = UpdateCustomerRequest(
            fullName = Customer.FullName("Should Fail"),
            preferredName = Customer.PreferredName("Should Fail Preferred Name"),
            emailAddress = Customer.EmailAddress("should-fail@example.com"),
            phoneNumber = Customer.PhoneNumber("+1234567890")
        )

        assertThat { subject.execute(originalCustomer.customerId, failedUpdateRequest) }
            .isFailure()
            .isInstanceOf(CustomerNotFoundException::class)
            .hasMessage("A customer with id `${originalCustomer.customerId.value}` was not found")
    }
}
