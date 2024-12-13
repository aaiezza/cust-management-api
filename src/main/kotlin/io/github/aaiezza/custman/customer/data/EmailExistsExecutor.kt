package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class EmailExistsExecutor(
    private val dslContext: DSLContext
) {
    // TODO: Add logging
    fun execute(emailAddress: Customer.EmailAddress): Boolean =
        dslContext.fetchExists(
            dslContext.selectFrom(CUSTOMER)
                .where(
                    CUSTOMER.EMAIL_ADDRESS.eq(emailAddress.value)
                        .and(CUSTOMER.DELETED_AT.isNull)
                )
        )
}
