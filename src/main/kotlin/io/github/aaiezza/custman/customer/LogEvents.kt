package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.klogging.BaseLogEvent
import io.github.aaiezza.klogging.error

class CustomerCreatedLogEvent(
    private val customer: Customer,
) : BaseLogEvent({
    it["customer_id"] = customer.customerId
})

class CustomerUpdatedLogEvent(
    private val customer: Customer,
) : BaseLogEvent({
    it["customer_id"] = customer.customerId
})

class CustomerDeleteLogEvent(
    private val customerId: Customer.Id,
) : BaseLogEvent({
    it["customer_id"] = customerId
})

class HandledExceptionLogEvent(
    private val customerId: Customer.Id? = null,
    private val throwable: Throwable,
    private val endpoint: String,
) : BaseLogEvent({
    customerId?.let { id -> it["customer_id"] = id }
    it["error_message"] = throwable.message.toString()
    it["endpoint"] = endpoint
    it["stack_trace"] = throwable.stackTraceToString()
})

fun CreateCustomerExceptionLogEvent(throwable: Throwable) = HandledExceptionLogEvent(throwable = throwable, endpoint = "create_customer")
fun GetCustomerExceptionLogEvent(customerId: Customer.Id? = null, throwable: Throwable) = HandledExceptionLogEvent(customerId, throwable, "get_customer")
fun UpdateCustomerExceptionLogEvent(customerId: Customer.Id? = null, throwable: Throwable) = HandledExceptionLogEvent(customerId, throwable, "update_customer")
fun DeleteCustomerExceptionLogEvent(customerId: Customer.Id? = null, throwable: Throwable) = HandledExceptionLogEvent(customerId, throwable, "delete_customer")

class UnhandledExceptionLogEvent(
    private val throwable: Throwable,
) : BaseLogEvent({
    it["error_message"] = throwable.message.toString()
    it["stack_trace"] = throwable.stackTraceToString()
})
