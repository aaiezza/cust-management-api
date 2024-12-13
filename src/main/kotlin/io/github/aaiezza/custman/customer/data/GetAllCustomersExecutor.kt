package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.Customers
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class GetAllCustomersExecutor(
    private val dslContext: DSLContext
) {
    fun execute(): Customers =
        dslContext.select()
            .from(CUSTOMER)
            .where(CUSTOMER.DELETED_AT.isNull())
            .orderBy(CUSTOMER.UPDATED_AT.desc())
            .fetch {
                Customer(
                    customerId = Customer.Id(it.getValue(CUSTOMER.CUSTOMER_ID)),
                    fullName = Customer.FullName(it.getValue(CUSTOMER.FULL_NAME)),
                    preferredName = Customer.PreferredName(it.getValue(CUSTOMER.PREFERRED_NAME)),
                    emailAddress = Customer.EmailAddress(it.getValue(CUSTOMER.EMAIL_ADDRESS)),
                    phoneNumber = Customer.PhoneNumber(it.getValue(CUSTOMER.PHONE_NUMBER)),
                    createdAt = Customer.CreatedAt(it.getValue(CUSTOMER.CREATED_AT)),
                    updatedAt = Customer.UpdatedAt(it.getValue(CUSTOMER.UPDATED_AT))
                )
            }.let(::Customers)
}

