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
                            .body(mapOf("error" to it.message))
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
                .body(mapOf("error" to ex.message))
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
                        val ex = CustomerNotFoundException(customerId)
                        UpdateCustomerExceptionLogEvent(
                            customerId,
                            ex,
                            HttpMethod.PUT,
                            "/customer/${customerId.value}"
                        ).error()
                        ResponseEntity.status(CONFLICT)
                            .body(mapOf("error" to ex.message))
                    }

                    is CustomerNotFoundException -> run {
                        val ex = CustomerNotFoundException(customerId)
                        UpdateCustomerExceptionLogEvent(
                            customerId,
                            ex,
                            HttpMethod.PUT,
                            "/customer/${customerId.value}"
                        ).error()
                        ResponseEntity.status(NOT_FOUND)
                            .body(mapOf("error" to ex.message))
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
            .body(mapOf("error_message" to exception.message))
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
            .body(mapOf("error_message" to "Malformed request: ${exception.message}"))
    }

//    // Handle argument validation errors
//    @ExceptionHandler(MethodArgumentNotValidException::class)
//    fun handleValidationException(exception: MethodArgumentNotValidException): ResponseEntity<*> {
//        val errors = exception.bindingResult.allErrors.map { error ->
//            (error as? FieldError)?.field to error.defaultMessage
//        }.toMap()
//
//        BadInputLogEvent(exception).error()
//        return ResponseEntity
//            .status(BAD_REQUEST)
//            .body(mapOf("error_message" to "Validation failed", "errors" to errors))
//    }
//
//    // Handle path variable or query parameter type mismatch
//    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
//    fun handleTypeMismatchException(exception: MethodArgumentTypeMismatchException): ResponseEntity<*> {
//        BadInputLogEvent(exception).error()
//        return ResponseEntity
//            .status(BAD_REQUEST)
//            .body(
//                mapOf(
//                    "error_message" to "Invalid value '${exception.value}' for parameter '${exception.name}'"
//                )
//            )
//    }
}
