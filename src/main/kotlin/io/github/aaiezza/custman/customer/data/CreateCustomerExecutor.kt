package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.toCustomerStub
import io.github.aaiezza.custman.jooq.generated.Tables
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class CreateCustomerExecutor(
    private val emailExistsExecutor: EmailExistsExecutor,
    private val dslContext: DSLContext
) {
    // TODO: Add logging
    fun execute(request: CreateCustomerRequest): Customer {
        if(emailExistsExecutor.execute(request.emailAddress)) {
            // TODO: This is a fine exception, but obfuscate this at the endpoint. No 409, because could be exploited to search emails.
            error { "Email address ${request.emailAddress} already exists" }
        }
        val customer = request.toCustomerStub()
            .let {
                dslContext
                    .insertInto(CUSTOMER)
                    .columns(
                        CUSTOMER.CUSTOMER_ID,
                        CUSTOMER.FULL_NAME,
                        CUSTOMER.PREFERRED_NAME,
                        CUSTOMER.EMAIL_ADDRESS,
                        CUSTOMER.PHONE_NUMBER
                    )
                    .values(
                        it.id.value,
                        it.fullName.value,
                        it.preferredName.value,
                        it.emailAddress.value,
                        it.phoneNumber.value
                    )
                    .returning(CUSTOMER.asterisk())
                    .fetchOneInto(CUSTOMER)
            }
            ?.let {
                Customer(
                    Customer.Id(it.customerId),
                    Customer.FullName(it.fullName),
                    Customer.PreferredName(it.preferredName),
                    Customer.EmailAddress(it.emailAddress),
                    Customer.PhoneNumber(it.phoneNumber),
                    Customer.CreatedAt(it.createdAt),
                    Customer.UpdatedAt(it.updatedAt),
                )
            }
            ?: run {
                // TODO: create custom exception and log
                throw IllegalStateException("Failed to insert customer")
            }

        // TODO: add logging
        return customer
    }
}
