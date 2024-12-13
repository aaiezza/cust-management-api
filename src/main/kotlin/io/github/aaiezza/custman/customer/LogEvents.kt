package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.klogging.BaseLogEvent

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

class UnhandledExceptionLogEvent(
    private val throwable: Throwable,
) : BaseLogEvent({
    it["error_message"] = throwable.message.toString()
})
