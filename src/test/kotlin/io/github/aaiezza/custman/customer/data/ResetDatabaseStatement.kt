package io.github.aaiezza.custman.customer.data

import io.github.aaiezza.custman.jooq.generated.Tables
import org.jooq.DSLContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("test")
class ResetDatabaseStatement(
    private val dslContext: DSLContext
) {
    fun execute() {
        dslContext
            .deleteFrom(Tables.CUSTOMER)
            .execute()
    }
}
