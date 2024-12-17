package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service

@Service
class SoftDeleteCustomerStatement(
    private val dslContext: DSLContext
) {
    private val logger = KotlinLogging.logger(SoftDeleteCustomerStatement::class.qualifiedName.toString())

    fun execute(customerId: Customer.Id) = execute(dslContext.configuration(), customerId)

    fun execute(configuration: Configuration, customerId: Customer.Id): Boolean {
        val rowsUpdated = configuration.dsl().update(CUSTOMER)
            .set(CUSTOMER.DELETED_AT, DSL.currentOffsetDateTime())
            .where(CUSTOMER.CUSTOMER_ID.eq(customerId.uuid).and(CUSTOMER.DELETED_AT.isNull))
            .let { statement ->
                logger.trace { statement.sql }
                statement.execute()
            }

        return rowsUpdated > 0
    }
}
