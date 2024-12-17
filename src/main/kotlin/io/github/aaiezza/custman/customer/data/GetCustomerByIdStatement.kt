package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jooq.Configuration
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class GetCustomerByIdStatement(
    private val dslContext: DSLContext
) {
    private val logger = KotlinLogging.logger(GetCustomerByIdStatement::class.qualifiedName.toString())

    fun execute(customerId: Customer.Id) = execute(dslContext.configuration(), customerId)

    fun execute(configuration: Configuration, customerId: Customer.Id): Customer? =
        configuration.dsl().select()
            .from(CUSTOMER)
            .where(CUSTOMER.CUSTOMER_ID.eq(customerId.uuid).and(CUSTOMER.DELETED_AT.isNull))
            .let { statement ->
                logger.trace { statement.sql }
                statement.fetchOneInto(CUSTOMER)?.toCustomer()
            }
}

