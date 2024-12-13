package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.data.CreateCustomerExecutor
import io.github.aaiezza.custman.customer.data.GetAllCustomersExecutor
import io.github.aaiezza.custman.customer.data.GetCustomerByIdExecutor
import io.github.aaiezza.custman.customer.data.SoftDeleteCustomerExecutor
import io.github.aaiezza.custman.customer.logevents.CustomerCreatedLogEvent
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.Customers
import io.github.aaiezza.klogging.info
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/customer")
class CustomerController(
    @Autowired private val getAllCustomersExecutor: GetAllCustomersExecutor,
    @Autowired private val createCustomerExecutor: CreateCustomerExecutor,
    @Autowired private val getCustomerByIdExecutor: GetCustomerByIdExecutor,
    @Autowired private val softDeleteCustomerExecutor: SoftDeleteCustomerExecutor,
) {
    @GetMapping
    fun getAllCustomers(): ResponseEntity<Customers> =
        getAllCustomersExecutor.execute()
            .let { ResponseEntity.ok(it) }

    @PostMapping
    fun createCustomer(@RequestBody request: CreateCustomerRequest): ResponseEntity<*> =
        runCatching { createCustomerExecutor.execute(request) }
            .map { newCustomer ->
                CustomerCreatedLogEvent(newCustomer).info()
                val location = URI.create("/customer/${newCustomer.customerId.value}")
                ResponseEntity.created(location).body(newCustomer)
            }
            .recover {
                when (it) {
                    is CustomerAlreadyExistsWithGivenEmailException -> ResponseEntity.status(CONFLICT)
                        .body(mapOf("error" to it.message))

                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to it.message))
                }
            }.getOrThrow()


    @GetMapping("/{customerId}")
    fun getCustomerById(@PathVariable customerId: String): ResponseEntity<Customer> =
        getCustomerByIdExecutor.execute(Customer.Id(UUID.fromString(customerId)))
            ?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{customerId}")
    fun deleteCustomer(@PathVariable customerId: String): ResponseEntity<Void> =
        softDeleteCustomerExecutor.execute(Customer.Id(UUID.fromString(customerId)))
            .let {
                if (it) {
                    ResponseEntity.noContent().build()
                } else {
                    ResponseEntity.notFound().build()
                }
            }
}

