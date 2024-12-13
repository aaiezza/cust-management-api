package io.github.aaiezza.custman.customer.logevents

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.klogging.BaseLogEvent

class CustomerCreatedLogEvent(
    private val customer: Customer,
) : BaseLogEvent({
    it["customer_id"] = customer.customerId
})
