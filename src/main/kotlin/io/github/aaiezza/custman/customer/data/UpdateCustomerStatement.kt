package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.CustomerAlreadyExistsWithGivenEmailException
import io.github.aaiezza.custman.customer.CustomerNotFoundException
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.UpdateCustomerRequest
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service

@Service
class UpdateCustomerStatement(
    private val getCustomerByIdStatement: GetCustomerByIdStatement,
    private val getCustomerByEmailAddressStatement: GetCustomerByEmailAddressStatement,
    private val dslContext: DSLContext
) {
    private val logger = KotlinLogging.logger(UpdateCustomerStatement::class.qualifiedName.toString())

    fun execute(customerId: Customer.Id, request: UpdateCustomerRequest) =
        execute(dslContext.configuration(), customerId, request)

    fun execute(configuration: Configuration, customerId: Customer.Id, request: UpdateCustomerRequest): Customer {
        return configuration.dsl().transactionResult { transaction ->
            val customerToUpdate = request.emailAddress?.let { emailAddress ->
                getCustomerByEmailAddressStatement.execute(transaction, emailAddress)
                    ?.also {
                        if (it.customerId != customerId)
                            throw CustomerAlreadyExistsWithGivenEmailException(emailAddress)
                    }
            } ?: getCustomerByIdStatement.execute(transaction, customerId) ?: throw CustomerNotFoundException(customerId)

            with(customerToUpdate) {
                copy(
                    fullName = request.fullName ?: fullName,
                    preferredName = request.preferredName ?: preferredName,
                    emailAddress = request.emailAddress ?: emailAddress,
                    phoneNumber = request.phoneNumber ?: phoneNumber,
                )
            }.let { customerWithUpdates ->
                with(customerWithUpdates) {
                    transaction.dsl()
                        .update(CUSTOMER)
                        .set(CUSTOMER.FULL_NAME, fullName.value)
                        .set(CUSTOMER.PREFERRED_NAME, preferredName.value)
                        .set(CUSTOMER.EMAIL_ADDRESS, emailAddress.value)
                        .set(CUSTOMER.PHONE_NUMBER, phoneNumber.value)
                        .set(CUSTOMER.UPDATED_AT, DSL.currentOffsetDateTime())
                        .where(CUSTOMER.CUSTOMER_ID.eq(customerId.uuid).and(CUSTOMER.DELETED_AT.isNull()))
                }
            }.let { statement ->
                logger.trace { statement.sql }
                statement.execute()
            }.let { modifiedRowCount ->
                if (modifiedRowCount <= 0) {
                    throw CustomerNotFoundException(customerId, IllegalStateException("Failed to update customer"))
                }
            }

            getCustomerByIdStatement.execute(transaction, customerId)
                ?: error { "Failed to get customer after update `${customerId.uuid}`" }
        }
    }
}
