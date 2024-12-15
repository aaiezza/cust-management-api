package io.github.aaiezza.custman.customer

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.github.aaiezza.custman.customer.data.*
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.Customers
import io.github.aaiezza.custman.customer.models.sample
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.*

class CustomerControllerTest {

    // Mock all required executors
    private val getAllCustomersExecutor: GetAllCustomersExecutor = mockk()
    private val createCustomerExecutor: CreateCustomerExecutor = mockk()
    private val getCustomerByIdExecutor: GetCustomerByIdExecutor = mockk()
    private val updateCustomerExecutor: UpdateCustomerExecutor = mockk()
    private val softDeleteCustomerExecutor: SoftDeleteCustomerExecutor = mockk()

    // Instantiate the controller with mocked executors
    private val customerController = CustomerController(
        getAllCustomersExecutor,
        createCustomerExecutor,
        getCustomerByIdExecutor,
        updateCustomerExecutor,
        softDeleteCustomerExecutor
    )

    @Test
    fun `createCustomer should return 201 CREATED with customer details`() {
        // Arrange
        val createRequest = CreateCustomerRequest.sample
        val createdCustomer = Customer.sample

        every { createCustomerExecutor.execute(createRequest) } returns createdCustomer

        // Act
        val response: ResponseEntity<*> = customerController.createCustomer(createRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isEqualTo(createdCustomer)

        // Verify
        verify(exactly = 1) { createCustomerExecutor.execute(createRequest) }
    }

    @Test
    fun `getAllCustomers should return 200 OK with customer list`() {
        // Arrange
        val customers = listOf(
            Customer.sample,
            Customer.sample.copy(customerId = Customer.Id(UUID.randomUUID()))
        ).let(::Customers)

        every { getAllCustomersExecutor.execute() } returns customers

        // Act
        val response: ResponseEntity<Customers> = customerController.getAllCustomers()

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(customers)

        // Verify
        verify(exactly = 1) { getAllCustomersExecutor.execute() }
    }

    @Test
    fun `getCustomerById should return 200 OK for valid customer ID`() {
        // Arrange
        val customerId = Customer.sample.customerId
        val customer = Customer.sample

        every { getCustomerByIdExecutor.execute(customerId) } returns customer

        // Act
        val response: ResponseEntity<Customer> = customerController.getCustomerById(customerId.value.toString())

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(customer)

        // Verify
        verify(exactly = 1) { getCustomerByIdExecutor.execute(customerId) }
    }

    @Test
    fun `getCustomerById should return 404 NOT FOUND for invalid customer ID`() {
        // Arrange
        val customerId = Customer.Id(UUID.randomUUID())

        every { getCustomerByIdExecutor.execute(customerId) } returns null

        // Act
        val response: ResponseEntity<Customer> = customerController.getCustomerById(customerId.value.toString())

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body).isNull()

        // Verify
        verify(exactly = 1) { getCustomerByIdExecutor.execute(customerId) }
    }

    @Test
    fun `softDeleteCustomer should return 204 NO CONTENT`() {
        // Arrange
        val customerId = Customer.sample.customerId

        every { softDeleteCustomerExecutor.execute(customerId) } returns true

        // Act
        val response: ResponseEntity<Void> = customerController.deleteCustomer(customerId.value.toString())

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // Verify
        verify(exactly = 1) { softDeleteCustomerExecutor.execute(customerId) }
    }
}
