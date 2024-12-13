package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class GetCustomerByIdExecutor(
    private val dslContext: DSLContext
) {
    fun execute(customerId: Customer.Id): Customer? =
        dslContext.select()
            .from(CUSTOMER)
            .where(CUSTOMER.CUSTOMER_ID.eq(customerId.value))
            .fetchOne {
                Customer(
                    id = Customer.Id(it.getValue(CUSTOMER.CUSTOMER_ID)),
                    fullName = Customer.FullName(it.getValue(CUSTOMER.FULL_NAME)),
                    preferredName = Customer.PreferredName(it.getValue(CUSTOMER.PREFERRED_NAME)),
                    emailAddress = Customer.EmailAddress(it.getValue(CUSTOMER.EMAIL_ADDRESS)),
                    phoneNumber = Customer.PhoneNumber(it.getValue(CUSTOMER.PHONE_NUMBER)),
                    createdAt = Customer.CreatedAt(it.getValue(CUSTOMER.CREATED_AT)),
                    updatedAt = Customer.UpdatedAt(it.getValue(CUSTOMER.UPDATED_AT))
                )
            }
}

