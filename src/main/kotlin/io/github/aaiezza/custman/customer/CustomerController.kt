package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.data.CreateCustomerExecutor
import io.github.aaiezza.custman.customer.data.GetCustomerByIdExecutor
import io.github.aaiezza.custman.customer.logevents.CustomerCreatedLogEvent
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.klogging.info
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/customer")
class CustomerController(
    @Autowired private val createCustomerExecutor: CreateCustomerExecutor,
    @Autowired private val getCustomerByIdExecutor: GetCustomerByIdExecutor,
) {

    @PostMapping
    fun createCustomer(@RequestBody request: CreateCustomerRequest): ResponseEntity<Customer> =
        createCustomerExecutor.execute(request)
            .let {
                CustomerCreatedLogEvent(it).info()
                val location = URI.create("/customer/${it.customerId.value}")
                ResponseEntity.created(location).body(it)
            }

    @GetMapping("/{customerId}")
    fun getCustomerById(@PathVariable customerId: String): ResponseEntity<Customer> =
        getCustomerByIdExecutor.execute(Customer.Id(UUID.fromString(customerId)))
            ?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
}

