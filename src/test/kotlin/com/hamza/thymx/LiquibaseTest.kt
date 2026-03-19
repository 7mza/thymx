package com.hamza.thymx

import org.junit.jupiter.api.Test
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest(properties = ["spring.jpa.hibernate.ddl-auto=validate", "spring.liquibase.enabled=true"])
@ActiveProfiles("default", "postgres")
@Import(PgTestContainer::class, TestConfs::class)
class LiquibaseTest {
    @Test
    fun `liquibase migration is valid`() {
    }
}
