package io.github.aaiezza.custman

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class CustomerManagementApiApplication

fun main(args: Array<String>) {
    runApplication<CustomerManagementApiApplication>(*args)
}
