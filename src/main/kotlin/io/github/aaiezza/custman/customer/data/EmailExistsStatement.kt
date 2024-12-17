package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jooq.Configuration
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class EmailExistsStatement(
    private val dslContext: DSLContext
) {
    private val logger = KotlinLogging.logger(EmailExistsStatement::class.qualifiedName.toString())

    fun execute(emailAddress: Customer.EmailAddress) = execute(dslContext.configuration(), emailAddress)

    // TODO: Add logging
    fun execute(configuration: Configuration, emailAddress: Customer.EmailAddress): Boolean =
        configuration.dsl().fetchExists(
            configuration.dsl().selectFrom(CUSTOMER)
                .where(
                    CUSTOMER.EMAIL_ADDRESS.eq(emailAddress.value)
                        .and(CUSTOMER.DELETED_AT.isNull)
                ).let { statement ->
                    logger.trace { statement.sql }
                    statement
                }
        )
}
