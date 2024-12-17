package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.Customers
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jooq.Configuration
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class GetAllCustomersStatement(
    private val dslContext: DSLContext
) {
    private val logger = KotlinLogging.logger(GetAllCustomersStatement::class.qualifiedName.toString())

    fun execute() = execute(dslContext.configuration())

    fun execute(configuration: Configuration): Customers =
        configuration.dsl().select()
            .from(CUSTOMER)
            .where(CUSTOMER.DELETED_AT.isNull())
            .orderBy(CUSTOMER.UPDATED_AT.desc()).let { statement ->
                logger.trace { statement.sql }
                statement.fetch { it.into(CUSTOMER).toCustomer() }.let(::Customers)
            }
}
