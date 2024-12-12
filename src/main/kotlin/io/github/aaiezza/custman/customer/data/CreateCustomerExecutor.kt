package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.toCustomerStub
import io.github.aaiezza.custman.jooq.generated.Tables
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class CreateCustomerExecutor(
    private val dslContext: DSLContext
) {
    // TODO: Add logging
    fun execute(request: CreateCustomerRequest): Customer {
        val customer = request.toCustomerStub()
            .let {
                dslContext
                    .insertInto(Tables.CUSTOMER)
                    .columns(
                        Tables.CUSTOMER.CUSTOMER_ID,
                        Tables.CUSTOMER.FULL_NAME,
                        Tables.CUSTOMER.PREFERRED_NAME,
                        Tables.CUSTOMER.EMAIL_ADDRESS,
                        Tables.CUSTOMER.PHONE_NUMBER
                    )
                    .values(
                        it.id.value,
                        it.fullName.value,
                        it.preferredName.value,
                        it.emailAddress.value,
                        it.phoneNumber.value
                    )
                    .returning(Tables.CUSTOMER.asterisk())
                    .fetchOneInto(Tables.CUSTOMER)
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
