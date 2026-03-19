package com.hamza.thymx.configs

import com.hamza.thymx.groups.Group
import com.hamza.thymx.groups.IGroupRepo
import com.hamza.thymx.users.Account
import com.hamza.thymx.users.Gender
import com.hamza.thymx.users.IUserRepo
import com.hamza.thymx.users.PhoneNumber
import com.hamza.thymx.users.Role
import com.hamza.thymx.users.User
import com.hamza.thymx.users.Username
import jakarta.transaction.Transactional
import net.datafaker.Faker
import org.h2.tools.Server
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.Random

@Configuration
class DevConfs(
    private val passwordEncoder: PasswordEncoder,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Profile("h2")
    @Bean(initMethod = "start", destroyMethod = "stop")
    fun h2TcpServer(
        @Value($$"${custom.h2.port}") port: String,
    ): Server {
        logger.warn("launching h2 in tcp mode")
        return Server.createTcpServer(
            "-tcp",
            "-tcpAllowOthers",
            "-tcpPort",
            port,
        )
    }

    @Profile("init")
    @Bean
    @Transactional
    fun initDB(
        securityProperties: SecurityProperties,
        faker: DataFaker,
        jdbcTemplate: JdbcTemplate,
        groupRepo: IGroupRepo,
        userRepo: IUserRepo,
    ) = ApplicationRunner {
        logger.debug("init: populating DB")
        jdbcTemplate.execute(
            """
            DELETE FROM spring_session_attributes;
            DELETE FROM spring_session;
            DELETE FROM "account_roles";
            DELETE FROM "users";
            DELETE FROM "groups";
            """.trimIndent(),
        )
        val groups = groupRepo.saveAllAndFlush(List(5) { faker.createFakeGroup() })
        val users = List(18) { faker.createFakeUser(groups = groups.toSet()) }
        val admin = faker.createFakeUser(groups = groups.toSet()).modifyWith(securityProperties.admin)
        val user = faker.createFakeUser(groups = groups.toSet()).modifyWith(securityProperties.user)
        userRepo.saveAllAndFlush(users.plus(admin).plus(user))
    }

    private fun User.modifyWith(user: com.hamza.thymx.configs.User): User =
        this.apply {
            email = user.username
            account.password = passwordEncoder.encode(user.password)!!
            account.authorities = user.roles.map { Role.valueOf(it.uppercase()) }.toHashSet()
        }
}

@Component
@Profile("init")
class DataFaker(
    private val passwordEncoder: PasswordEncoder,
) {
    private val faker: Faker = Faker(Random(0))

    fun createFakeGroup(): Group = Group(name = faker.country().capital())

    fun createFakeUser(groups: Set<Group>? = null): User {
        val username =
            Username(
                firstName = faker.name().firstName(),
                lastName = faker.name().lastName(),
            )
        return User(
            username = username,
            gender = faker.options().option(Gender::class.java),
            birthday = faker.timeAndDate().birthday(),
            email = faker.internet().emailAddress(username.toString()),
            phoneNumber =
                PhoneNumber(
                    prefix = "+${faker.number().digits(3)}",
                    num = "${faker.number().digits(10)}",
                ),
            group = groups?.random(),
            account =
                Account(
                    password = passwordEncoder.encode(faker.credentials().password(8, 20))!!,
                    authorities = hashSetOf(faker.options().option(Role::class.java)),
                ),
        )
    }
}
