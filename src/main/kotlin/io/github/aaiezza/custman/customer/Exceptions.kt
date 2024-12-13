package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.models.Customer

class CustomerAlreadyExistsWithGivenEmailException(emailAddress: Customer.EmailAddress, cause: Throwable? = null) :
    Exception("A customer with email `${emailAddress.value}` already exists", cause)
