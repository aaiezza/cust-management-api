package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.klogging.BaseLogEvent
import org.springframework.http.HttpMethod

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

abstract class HttpRequestException(
    private val throwable: Throwable,
    private val method: HttpMethod,
    private val endpoint: String,
    private val additionalFields: ((MutableMap<String, Any>) -> Unit)? = null
) : BaseLogEvent({
    it["error_message"] = throwable.message.toString()
    it["method"] = method
    it["endpoint"] = endpoint
    it["stack_trace"] = throwable.stackTraceToString()
    additionalFields?.invoke(it)
})

class HandledExceptionLogEvent(
    private val customerId: Customer.Id? = null,
    throwable: Throwable,
    method: HttpMethod,
    endpoint: String,
) : HttpRequestException(throwable, method, endpoint, {
    customerId?.let { id -> it["customer_id"] = id }
})

class MalformedInputExceptionLogEvent(
    throwable: Throwable,
    method: HttpMethod,
    endpoint: String,
) : HttpRequestException(throwable, method, endpoint)

fun CreateCustomerExceptionLogEvent(throwable: Throwable, method: HttpMethod, endpoint: String) =
    HandledExceptionLogEvent(throwable = throwable, method = method, endpoint = endpoint)

fun GetCustomerExceptionLogEvent(
    customerId: Customer.Id? = null,
    throwable: Throwable,
    method: HttpMethod,
    endpoint: String
) =
    HandledExceptionLogEvent(customerId, throwable, method, endpoint)

fun UpdateCustomerExceptionLogEvent(
    customerId: Customer.Id? = null,
    throwable: Throwable,
    method: HttpMethod,
    endpoint: String
) =
    HandledExceptionLogEvent(customerId, throwable, method, endpoint)

fun DeleteCustomerExceptionLogEvent(
    customerId: Customer.Id? = null,
    throwable: Throwable,
    method: HttpMethod,
    endpoint: String
) =
    HandledExceptionLogEvent(customerId, throwable, method, endpoint)

class UnhandledExceptionLogEvent(
    private val throwable: Throwable,
    private val method: HttpMethod,
    private val endpoint: String,
) : BaseLogEvent({
    it["error_message"] = throwable.message.toString()
    it["method"] = method
    it["endpoint"] = endpoint
    it["stack_trace"] = throwable.stackTraceToString()
})
