package io.github.aaiezza.custman.spring.config

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import javax.sql.DataSource

@Configuration
open class JooqConfig {
    @Bean
    open fun dslContext(dataSource: DataSource): DSLContext {
        return DSL.using(dataSource, org.jooq.SQLDialect.POSTGRES)
    }
}
