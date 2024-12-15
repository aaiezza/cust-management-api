package io.github.aaiezza.custman.customer.data

import assertk.assertThat
import assertk.assertions.isTrue
import io.github.aaiezza.custman.customer.models.Customer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class EmailExistsExecutorIT {

    @Autowired
    private lateinit var subject: EmailExistsExecutor

    @Test
    fun testExecutorProcessesMessageAndSavesToDatabase() {
        val response = subject.execute(Customer.EmailAddress("asdf@example.com"))
        assertThat(response).isTrue()
    }
}
