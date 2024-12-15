package io.github.aaiezza.custman.customer.data

import assertk.assertThat
import assertk.assertions.isFalse
import io.github.aaiezza.custman.customer.models.Customer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class EmailExistsStatementIT {

    @Autowired
    private lateinit var subject: EmailExistsStatement

    @Test
    fun testExecutorProcessesMessageAndSavesToDatabase() {
        val response = subject.execute(Customer.EmailAddress("asdf@example.com"))
        assertThat(response).isFalse()
    }
}
