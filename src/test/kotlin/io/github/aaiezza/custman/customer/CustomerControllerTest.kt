package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.data.*
import io.github.aaiezza.custman.customer.models.*
import io.mockk.*
import org.junit.jupiter.api.*
import org.springframework.http.ResponseEntity
import java.util.*
import kotlin.test.assertEquals

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
  assertEquals(201, response.statusCodeValue)
  assertEquals(createdCustomer, response.body)

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
  assertEquals(200, response.statusCodeValue)
  assertEquals(customers, response.body)

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
  assertEquals(200, response.statusCodeValue)
  assertEquals(customer, response.body)

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
  assertEquals(404, response.statusCodeValue)
  assertEquals(null, response.body)

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
  assertEquals(204, response.statusCodeValue)

  // Verify
  verify(exactly = 1) { softDeleteCustomerExecutor.execute(customerId) }
 }
}
