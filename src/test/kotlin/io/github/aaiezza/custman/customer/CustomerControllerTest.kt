package io.github.aaiezza.custman.customer

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.aaiezza.custman.customer.data.*
import io.github.aaiezza.custman.customer.models.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.*

class CustomerControllerTest {

    // Mock all required executors
    private val getAllCustomersStatement: GetAllCustomersStatement = mockk()
    private val createCustomerStatement: CreateCustomerStatement = mockk()
    private val getCustomerByIdStatement: GetCustomerByIdStatement = mockk()
    private val updateCustomerStatement: UpdateCustomerStatement = mockk()
    private val softDeleteCustomerStatement: SoftDeleteCustomerStatement = mockk()

    // Instantiate the controller with mocked executors
    private val customerController = CustomerController(
        getAllCustomersStatement,
        createCustomerStatement,
        getCustomerByIdStatement,
        updateCustomerStatement,
        softDeleteCustomerStatement
    )

    @Test
    fun `createCustomer should return 201 CREATED with customer details`() {
        // Arrange
        val createRequest = CreateCustomerRequest.sample
        val createdCustomer = Customer.sample

        every { createCustomerStatement.execute(createRequest) } returns createdCustomer

        // Act
        val response: ResponseEntity<*> = customerController.createCustomer(createRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isEqualTo(createdCustomer)

        // Verify
        verify(exactly = 1) { createCustomerStatement.execute(createRequest) }
    }

    @Test
    fun `getAllCustomers should return 200 OK with customer list`() {
        // Arrange
        val customers = listOf(
            Customer.sample,
            Customer.sample.copy(customerId = Customer.Id(UUID.randomUUID()))
        ).let(::Customers)

        every { getAllCustomersStatement.execute() } returns customers

        // Act
        val response: ResponseEntity<Customers> = customerController.getAllCustomers()

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(customers)

        // Verify
        verify(exactly = 1) { getAllCustomersStatement.execute() }
    }

    @Test
    fun `getCustomerById should return 200 OK for valid customer ID`() {
        // Arrange
        val customerId = Customer.sample.customerId
        val customer = Customer.sample

        every { getCustomerByIdStatement.execute(customerId) } returns customer

        // Act
        val response: ResponseEntity<*> = customerController.getCustomerById(customerId.value.toString())

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(customer)

        // Verify
        verify(exactly = 1) { getCustomerByIdStatement.execute(customerId) }
    }

    @Test
    fun `getCustomerById should return 404 NOT FOUND for invalid customer ID`() {
        // Arrange
        val customerId = Customer.Id(UUID.randomUUID())

        every { getCustomerByIdStatement.execute(customerId) } returns null

        // Act
        val response: ResponseEntity<*> = customerController.getCustomerById(customerId.value.toString())

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body).isEqualTo(mapOf("error" to CustomerNotFoundException(customerId).message))

        // Verify
        verify(exactly = 1) { getCustomerByIdStatement.execute(customerId) }
    }

    @Test
    fun `softDeleteCustomer should return 204 NO CONTENT`() {
        // Arrange
        val customerId = Customer.sample.customerId

        every { softDeleteCustomerStatement.execute(customerId) } returns true

        // Act
        val response: ResponseEntity<Void> = customerController.deleteCustomer(customerId.value.toString())

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // Verify
        verify(exactly = 1) { softDeleteCustomerStatement.execute(customerId) }
    }

    @Test
    fun `updateCustomer should return 200 OK with updated customer details`() {
        // Arrange
        val customerId = Customer.sample.customerId
        val updateRequest = UpdateCustomerRequest.sample
        val updatedCustomer = Customer.sample.copy(fullName = Customer.FullName("Updated Name"))

        every { getCustomerByIdStatement.execute(customerId) } returns Customer.sample
        every { updateCustomerStatement.execute(customerId, updateRequest) } returns updatedCustomer

        // Act
        val response: ResponseEntity<*> = customerController.updateCustomer(customerId.value.toString(), updateRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(updatedCustomer)

        // Verify
        verify(exactly = 1) { getCustomerByIdStatement.execute(customerId) }
        verify(exactly = 1) { updateCustomerStatement.execute(customerId, updateRequest) }
    }

    @Test
    fun `updateCustomer should return 404 NOT FOUND for non-existent customer`() {
        // Arrange
        val customerId = Customer.Id(UUID.randomUUID())
        val updateRequest = UpdateCustomerRequest.sample

        every { getCustomerByIdStatement.execute(customerId) } returns null

        // Act
        val response: ResponseEntity<*> = customerController.updateCustomer(customerId.value.toString(), updateRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body).isEqualTo(mapOf("error" to CustomerNotFoundException(customerId).message))

        // Verify
        verify(exactly = 1) { getCustomerByIdStatement.execute(customerId) }
        verify(exactly = 0) { updateCustomerStatement.execute(customerId, updateRequest) }
    }
}
