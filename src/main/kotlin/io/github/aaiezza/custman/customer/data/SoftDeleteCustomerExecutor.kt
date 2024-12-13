package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service

@Service
class SoftDeleteCustomerExecutor(
    private val dslContext: DSLContext
) {
    fun execute(customerId: Customer.Id): Boolean {
        val rowsUpdated = dslContext.update(CUSTOMER)
            .set(CUSTOMER.DELETED_AT, DSL.currentOffsetDateTime())
            .where(CUSTOMER.CUSTOMER_ID.eq(customerId.value).and(CUSTOMER.DELETED_AT.isNull))
            .execute()

        return rowsUpdated > 0
    }
}
