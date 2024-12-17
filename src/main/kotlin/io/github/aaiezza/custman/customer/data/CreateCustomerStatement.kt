package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.customer.CustomerAlreadyExistsWithGivenEmailException
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.custman.customer.models.Customer
import io.github.aaiezza.custman.customer.models.toCustomerStub
import io.github.aaiezza.custman.jooq.generated.Tables.CUSTOMER
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jooq.Configuration
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class CreateCustomerStatement(
    private val emailExistsStatement: EmailExistsStatement,
    private val dslContext: DSLContext
) {
    private val logger = KotlinLogging.logger(CreateCustomerStatement::class.qualifiedName.toString())

    fun execute(request: CreateCustomerRequest): Customer = execute(dslContext.configuration(), request)

    fun execute(configuration: Configuration, request: CreateCustomerRequest): Customer {
        if (emailExistsStatement.execute(configuration, request.emailAddress)) {
            throw CustomerAlreadyExistsWithGivenEmailException(request.emailAddress)
        }
        val customer = request.toCustomerStub()
            .let {
                configuration.dsl()
                    .insertInto(CUSTOMER)
                    .columns(
                        CUSTOMER.CUSTOMER_ID,
                        CUSTOMER.FULL_NAME,
                        CUSTOMER.PREFERRED_NAME,
                        CUSTOMER.EMAIL_ADDRESS,
                        CUSTOMER.PHONE_NUMBER
                    )
                    .values(
                        it.id.uuid,
                        it.fullName.value,
                        it.preferredName.value,
                        it.emailAddress.value,
                        it.phoneNumber.value
                    )
                    .returning(CUSTOMER.asterisk()).let { statement ->
                        logger.trace { statement.sql }
                        statement.fetchOneInto(CUSTOMER)
                    }
            }
            ?.let {
                Customer(
                    Customer.Id(it.customerId),
                    Customer.FullName(it.fullName),
                    Customer.PreferredName(it.preferredName),
                    Customer.EmailAddress(it.emailAddress),
                    Customer.PhoneNumber(it.phoneNumber),
                    Customer.CreatedAt(it.createdAt),
                    Customer.UpdatedAt(it.updatedAt),
                )
            }
            ?: run {
                error { "Failed to insert customer" }
            }

        return customer
    }
}
