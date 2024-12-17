package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.data.*
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.Customers
import io.github.aaiezza.custman.customer.models.UpdateCustomerRequest
import io.github.aaiezza.klogging.error
import io.github.aaiezza.klogging.info
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*
import java.net.URI

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
                val location = URI.create("/customer/${newCustomer.customerId.uuid}")
                ResponseEntity.created(location).body(newCustomer)
            }
            .recover {
                when (it) {
                    is CustomerAlreadyExistsWithGivenEmailException -> run {
                        CreateCustomerExceptionLogEvent(it, HttpMethod.POST, "/customer").error()
                        ResponseEntity.status(CONFLICT)
                            .errorMessageBody(it)
                    }

                    else -> throw it
                }
            }.getOrThrow()

    @GetMapping("/{customerId}")
    fun getCustomerById(@PathVariable("customerId") customerId: Customer.Id): ResponseEntity<*> =
        getCustomerByIdStatement.execute(customerId)
            ?.let { customer -> ResponseEntity.ok(customer) } ?: run {
            val ex = CustomerNotFoundException(customerId)
            GetCustomerExceptionLogEvent(customerId, ex, HttpMethod.GET, "/customer/${customerId.value}").error()
            ResponseEntity.status(NOT_FOUND)
                .errorMessageBody(ex)
        }

    @PutMapping("/{customerId}")
    fun updateCustomer(
        @PathVariable("customerId") customerId: Customer.Id,
        @RequestBody request: UpdateCustomerRequest
    ): ResponseEntity<*> {
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
                        UpdateCustomerExceptionLogEvent(
                            customerId,
                            it,
                            HttpMethod.PUT,
                            "/customer/${customerId.value}"
                        ).error()
                        ResponseEntity.status(CONFLICT)
                            .errorMessageBody(it)
                    }

                    is CustomerNotFoundException -> run {
                        UpdateCustomerExceptionLogEvent(
                            customerId,
                            it,
                            HttpMethod.PUT,
                            "/customer/${customerId.value}"
                        ).error()
                        ResponseEntity.status(NOT_FOUND)
                            .errorMessageBody(it)
                    }

                    else -> throw it
                }
            }.getOrThrow()
    }

    @DeleteMapping("/{customerId}")
    fun deleteCustomer(@PathVariable("customerId") customerId: Customer.Id): ResponseEntity<Void> {
        return softDeleteCustomerStatement.execute(customerId)
            .let {
                if (it) {
                    CustomerDeleteLogEvent(customerId).info()
                    ResponseEntity.noContent().build()
                } else {
                    DeleteCustomerExceptionLogEvent(
                        customerId,
                        CustomerNotFoundException(customerId),
                        HttpMethod.DELETE, "/customer/${customerId.value}"
                    ).error()
                    ResponseEntity.notFound().build()
                }
            }
    }
}

@RestControllerAdvice
class GlobalExceptionHandler {
    // Handle generic unhandled exceptions
    @ExceptionHandler(Exception::class)
    fun handleGenericException(exception: Exception, request: HttpServletRequest): ResponseEntity<*> {
        UnhandledExceptionLogEvent(exception, request.method.let(HttpMethod::valueOf), request.contextPath).error()
        return ResponseEntity
            .status(INTERNAL_SERVER_ERROR)
            .errorMessageBody(exception)
    }

    // Handle bad user input - JSON format issues
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseError(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        MalformedInputExceptionLogEvent(exception, request.method.let(HttpMethod::valueOf), request.contextPath).error()
        return ResponseEntity
            .status(BAD_REQUEST)
            .errorMessageBody(exception)
    }
}

fun ResponseEntity.BodyBuilder.errorMessageBody(ex: Exception): ResponseEntity<*> =
    body(mapOf("error_message" to "${ex.message}"))
