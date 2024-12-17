package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import io.github.aaiezza.custman.jooq.generated.tables.records.CustomerRecord

fun CustomerRecord.toCustomer() =
    io.github.aaiezza.custman.customer.models.Customer(
        customerId = io.github.aaiezza.custman.customer.models.Customer.Id(getValue(CUSTOMER.CUSTOMER_ID)),
        fullName = io.github.aaiezza.custman.customer.models.Customer.FullName(getValue(CUSTOMER.FULL_NAME)),
        preferredName = io.github.aaiezza.custman.customer.models.Customer.PreferredName(getValue(CUSTOMER.PREFERRED_NAME)),
        emailAddress = io.github.aaiezza.custman.customer.models.Customer.EmailAddress(getValue(CUSTOMER.EMAIL_ADDRESS)),
        phoneNumber = io.github.aaiezza.custman.customer.models.Customer.PhoneNumber(getValue(CUSTOMER.PHONE_NUMBER)),
        createdAt = io.github.aaiezza.custman.customer.models.Customer.CreatedAt(getValue(CUSTOMER.CREATED_AT)),
        updatedAt = io.github.aaiezza.custman.customer.models.Customer.UpdatedAt(getValue(CUSTOMER.UPDATED_AT))
    )
