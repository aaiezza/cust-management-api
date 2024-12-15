package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.CustomerAlreadyExistsWithGivenEmailException
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.UpdateCustomerRequest
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class UpdateCustomerStatement(
    private val getCustomerByIdStatement: GetCustomerByIdStatement,
    private val getCustomerByEmailAddressStatement: GetCustomerByEmailAddressStatement,
    private val dslContext: DSLContext
) {
    // TODO: Add logging
    fun execute(customerId: Customer.Id, request: UpdateCustomerRequest): Customer {
        val existingCustomer = getCustomerByEmailAddressStatement.execute(request.emailAddress)

        if (existingCustomer != null && existingCustomer.customerId != customerId) {
            throw CustomerAlreadyExistsWithGivenEmailException(request.emailAddress)
        }
        with(request) {
            dslContext
                .update(CUSTOMER)
                .set(CUSTOMER.FULL_NAME, fullName.value)
                .set(CUSTOMER.PREFERRED_NAME, preferredName.value)
                .set(CUSTOMER.EMAIL_ADDRESS, emailAddress.value)
                .set(CUSTOMER.PHONE_NUMBER, phoneNumber.value)
                .where(CUSTOMER.CUSTOMER_ID.eq(customerId.value).and(CUSTOMER.DELETED_AT.isNull()))
                .execute()
        }.let {
            if (it <= 0) {
                error("Failed to update customer `${customerId.value}`")
            }
        }

        // TODO: add logging
        return getCustomerByIdStatement.execute(customerId)
            ?: error { "Failed to get customer after update `${customerId.value}`" }
    }
}
