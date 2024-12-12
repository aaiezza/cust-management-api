package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.data.CreateCustomerExecutor
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/customer")
class CustomerController(
    @Autowired private val createCustomerExecutor: CreateCustomerExecutor
) {

    @PostMapping
    fun createCustomer(@RequestBody request: CreateCustomerRequest): ResponseEntity<Customer> {
        val createdCustomer = createCustomerExecutor.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer)
    }
}

