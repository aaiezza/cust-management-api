package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.toCustomer
import io.github.aaiezza.custman.jooq.generated.Tables
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.util.*

@Service
class CreateCustomerExecutor(
    private val dslContext: DSLContext
) {
    // TODO: Add logging
    fun execute(request: CreateCustomerRequest): Customer {
        val customer = request.toCustomer()
        val dbid = dslContext
            .insertInto(Tables.CUSTOMER)
            .columns(
                Tables.CUSTOMER.CUSTOMER_ID,
                Tables.CUSTOMER.FULL_NAME,
                Tables.CUSTOMER.PREFERRED_NAME,
                Tables.CUSTOMER.EMAIL_ADDRESS,
                Tables.CUSTOMER.PHONE_NUMBER
            )
            .values(customer.id.value, customer.fullName.value, customer.preferredName.value, customer.emailAddress.value, customer.phoneNumber.value)
            .returning(Tables.CUSTOMER.CUSTOMER_DBID)
            .fetchOne()
            ?.getValue(Tables.CUSTOMER.CUSTOMER_DBID)
            ?: throw IllegalStateException("Failed to insert customer")
        // TODO: create custom exception

        return customer
    }
}
