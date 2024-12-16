package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.data.*
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.Customers
import io.github.aaiezza.custman.customer.models.UpdateCustomerRequest
import io.github.aaiezza.klogging.error
import io.github.aaiezza.klogging.info
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/customer")
class CustomerController(
    @Autowired private val getAllCustomersStatement: GetAllCustomersStatement,
    @Autowired private val createCustomerStatement: CreateCustomerStatement,
    @Autowired private val getCustomerByIdStatement: GetCustomerByIdStatement,
    @Autowired private val updateCustomerStatement: UpdateCustomerStatement,
    @Autowired private val softDeleteCustomerStatement: SoftDeleteCustomerStatement,
) {
    @GetMapping
    fun getAllCustomers(): ResponseEntity<Customers> =
        getAllCustomersStatement.execute()
            .let { ResponseEntity.ok(it) }

    @PostMapping
    fun createCustomer(@RequestBody request: CreateCustomerRequest): ResponseEntity<*> =
        runCatching { createCustomerStatement.execute(request) }
            .map { newCustomer ->
                CustomerCreatedLogEvent(newCustomer).info()
                val location = URI.create("/customer/${newCustomer.customerId.value}")
                ResponseEntity.created(location).body(newCustomer)
            }
            .recover {
                when (it) {
                    is CustomerAlreadyExistsWithGivenEmailException -> run {
                        CreateCustomerExceptionLogEvent(throwable = it).error()
                        ResponseEntity.status(CONFLICT)
                            .body(mapOf("error" to it.message))
                    }

                    else -> throw it
                }
            }.getOrThrow()

    @GetMapping("/{customerId}")
    fun getCustomerById(@PathVariable("customerId") customerIdString: String): ResponseEntity<*> =
        Customer.Id(UUID.fromString(customerIdString))
            .let { customerId ->
                getCustomerByIdStatement.execute(customerId)
                    ?.let { customer -> ResponseEntity.ok(customer) } ?: run {
                    val ex = CustomerNotFoundException(customerId)
                    GetCustomerExceptionLogEvent(customerId, ex).error()
                    ResponseEntity.status(NOT_FOUND)
                        .body(mapOf("error" to ex.message))
                }
            }

    @PutMapping("/{customerId}")
    fun updateCustomer(
        @PathVariable("customerId") customerIdString: String,
        @RequestBody request: UpdateCustomerRequest
    ): ResponseEntity<*> {
        val customerId = Customer.Id(UUID.fromString(customerIdString))
        return runCatching {
            getCustomerByIdStatement.execute(customerId)
                ?.let {
                    updateCustomerStatement.execute(it.customerId, request)
                } ?: throw CustomerNotFoundException(customerId)
        }
            .map { updatedCustomer ->
                CustomerUpdatedLogEvent(updatedCustomer).info()
                ResponseEntity.ok(updatedCustomer)
            }
            .recover {
                when (it) {
                    is CustomerAlreadyExistsWithGivenEmailException -> run {
                        val ex = CustomerNotFoundException(customerId)
                        UpdateCustomerExceptionLogEvent(customerId, ex).error()
                        ResponseEntity.status(CONFLICT)
                            .body(mapOf("error" to ex.message))
                    }

                    is CustomerNotFoundException -> run {
                        val ex = CustomerNotFoundException(customerId)
                        UpdateCustomerExceptionLogEvent(customerId, ex).error()
                        ResponseEntity.status(NOT_FOUND)
                            .body(mapOf("error" to ex.message))
                    }

                    else -> throw it
                }
            }.getOrThrow()
    }

    @DeleteMapping("/{customerId}")
    fun deleteCustomer(@PathVariable("customerId") customerIdString: String): ResponseEntity<Void> {
        val customerId = Customer.Id(UUID.fromString(customerIdString))
        return softDeleteCustomerStatement.execute(customerId)
            .let {
                if (it) {
                    CustomerDeleteLogEvent(customerId).info()
                    ResponseEntity.noContent().build()
                } else {
                    DeleteCustomerExceptionLogEvent(
                        customerId,
                        CustomerNotFoundException(customerId),
                    ).error()
                    ResponseEntity.notFound().build()
                }
            }
    }
}

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleConflictException(exception: Exception): ResponseEntity<*> {
        UnhandledExceptionLogEvent(exception).error()
        return ResponseEntity
            .status(INTERNAL_SERVER_ERROR)
            .body(mapOf("error_message" to exception.message))
    }
}
