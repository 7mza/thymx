package com.hamza.thymx

import com.hamza.thymx.configs.DataFaker
import com.hamza.thymx.groups.IGroupRepo
import com.hamza.thymx.users.IUserRepo
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant

@DataJpaTest
@Import(PgTestContainer::class, TestConfs::class)
class DataTest {
    @Autowired
    private lateinit var userRepo: IUserRepo

    @Autowired
    private lateinit var groupRepo: IGroupRepo

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var faker: DataFaker

    @BeforeEach
    fun beforeEach() {
        assertThat(groupRepo.count()).isZero
        assertThat(userRepo.count()).isZero

        faker = DataFaker(passwordEncoder)
    }

    @AfterEach
    fun afterEach() {
        // not needed with @DataJpaTest
        // groupRepository.deleteAll()
        // userRepository.deleteAll()
    }

    @Test
    fun `save user without group`() {
        val user = faker.createFakeUser()
        userRepo.saveAndFlush(user)

        entityManager.flush()
        entityManager.clear()

        assertThat(groupRepo.count()).isZero
        assertThat(userRepo.count()).isOne

        val saved = userRepo.findById(user.id).orElseThrow()

        assertThat(saved.id).isEqualTo(user.id)
        assertThat(saved.username).isEqualTo(user.username)
        assertThat(saved.gender).isEqualTo(user.gender)
        assertThat(saved.birthday).isEqualTo(user.birthday)
        assertThat(saved.email).isEqualTo(user.email)
        assertThat(saved.phoneNumber).isEqualTo(user.phoneNumber)
        assertThat(saved.group).isNull()
        assertThat(saved.createdAt).isBefore(Instant.now())
        assertThat(saved.updatedAt).isAfter(saved.createdAt)
    }

    @Test
    fun `save user with existing group, should reflect in user and in group`() {
        // save group without user
        val group = faker.createFakeGroup()
        val savedGroup = groupRepo.saveAndFlush(group)

        // detach
        entityManager.flush()
        entityManager.clear()

        assertThat(groupRepo.count()).isOne
        assertThat(userRepo.count()).isZero

        // check group without user
        assertThat(savedGroup.id).isEqualTo(group.id)
        assertThat(savedGroup.users).isEmpty()

        // associate from owning side
        // save user with previous group
        val user = faker.createFakeUser(groups = setOf(group))
        userRepo.saveAndFlush(user)

        // detach
        entityManager.flush()
        entityManager.clear()

        assertThat(groupRepo.count()).isOne
        assertThat(userRepo.count()).isOne

        // reattach
        val retrievedUser = userRepo.findById(user.id).orElseThrow()

        assertThat(retrievedUser.id).isEqualTo(user.id)
        assertThat(retrievedUser.username).isEqualTo(user.username)
        assertThat(retrievedUser.gender).isEqualTo(user.gender)
        assertThat(retrievedUser.birthday).isEqualTo(user.birthday)
        assertThat(retrievedUser.email).isEqualTo(user.email)
        assertThat(retrievedUser.phoneNumber).isEqualTo(user.phoneNumber)
        assertThat(retrievedUser.createdAt).isBefore(Instant.now())
        assertThat(retrievedUser.updatedAt).isAfter(retrievedUser.createdAt)

        // check group can be retrieved from user
        assertThat(retrievedUser.group).isNotNull
        val innerGroup = retrievedUser.group!!
        assertThat(innerGroup.id).isEqualTo(group.id)
        assertThat(innerGroup.name).isEqualTo(group.name)
        assertThat(innerGroup.users.size).isOne
        assertThat(innerGroup.createdAt).isBefore(Instant.now())
        assertThat(innerGroup.updatedAt).isAfter(innerGroup.createdAt)

        // reattach
        // retrieve previous group
        val retrievedGroup = groupRepo.findById(group.id).orElseThrow()

        // check user can be retrieved from group
        assertThat(retrievedGroup.users.size).isOne
        assertThat(retrievedGroup.users.first().id).isEqualTo(user.id)
    }

    @Test
    fun `save group without user`() {
        val group = faker.createFakeGroup()
        groupRepo.saveAndFlush(group)

        entityManager.flush()
        entityManager.clear()

        assertThat(groupRepo.count()).isOne
        assertThat(userRepo.count()).isZero

        val saved = groupRepo.findById(group.id).orElseThrow()

        assertThat(saved.id).isEqualTo(group.id)
        assertThat(saved.name).isEqualTo(group.name)
        assertThat(saved.users).isEmpty()
        assertThat(saved.createdAt).isBefore(Instant.now())
        assertThat(saved.updatedAt).isAfter(saved.createdAt)
    }

    @Test
    fun `save group with existing user, should reflect in group and in user`() {
        // save user without group
        val user = faker.createFakeUser()
        val savedUser = userRepo.saveAndFlush(user)

        // detach
        entityManager.flush()
        entityManager.clear()

        assertThat(groupRepo.count()).isZero
        assertThat(userRepo.count()).isOne

        // check user without group
        assertThat(savedUser.id).isEqualTo(user.id)
        assertThat(savedUser.group).isNull()

        // reattach
        var retrievedUser = userRepo.findById(user.id).orElseThrow()

        // associate from non owning side
        // save group with previous user
        val group = faker.createFakeGroup()
        group.addUser(retrievedUser) // FIXME: non owning need this
        groupRepo.saveAndFlush(group)

        // detach
        entityManager.flush()
        entityManager.clear()

        assertThat(groupRepo.count()).isOne
        assertThat(userRepo.count()).isOne

        // reattach
        val retrievedGroup = groupRepo.findById(group.id).orElseThrow()

        assertThat(retrievedGroup.id).isEqualTo(group.id)
        assertThat(retrievedGroup.name).isEqualTo(group.name)
        assertThat(retrievedGroup.createdAt).isBefore(Instant.now())
        assertThat(retrievedGroup.updatedAt).isAfter(retrievedGroup.createdAt)

        // check user can be retrieved from group
        assertThat(retrievedGroup.users.size).isOne
        val innerUser = retrievedGroup.users.first()
        assertThat(innerUser.id).isEqualTo(user.id)
        assertThat(innerUser.username).isEqualTo(user.username)
        assertThat(innerUser.gender).isEqualTo(user.gender)
        assertThat(innerUser.birthday).isEqualTo(user.birthday)
        assertThat(innerUser.email).isEqualTo(user.email)
        assertThat(innerUser.phoneNumber).isEqualTo(user.phoneNumber)
        assertThat(innerUser.group).isNotNull
        assertThat(innerUser.createdAt).isBefore(Instant.now())
        assertThat(innerUser.updatedAt).isAfter(innerUser.createdAt)

        // reattach
        // retrieve previous user
        retrievedUser = userRepo.findById(user.id).orElseThrow()

        // check group can be retrieved from user
        assertThat(retrievedUser.group).isNotNull
        assertThat(retrievedUser.group!!.id).isEqualTo(group.id)
    }

    @Test
    fun `delete user should reflect on group`() {
        val group = faker.createFakeGroup()
        val savedGroup = groupRepo.saveAndFlush(group)
        val user = faker.createFakeUser(groups = setOf(group))
        val savedUser = userRepo.saveAndFlush(user)

        entityManager.flush()
        entityManager.clear()

        assertThat(groupRepo.count()).isOne
        assertThat(userRepo.count()).isOne

        userRepo.deleteById(user.id)

        entityManager.flush()
        entityManager.clear()

        assertThat(groupRepo.count()).isOne
        assertThat(userRepo.count()).isZero

        val retrievedGroup = groupRepo.findById(group.id).orElseThrow()

        assertThat(retrievedGroup.id).isEqualTo(group.id)
        assertThat(retrievedGroup.name).isEqualTo(group.name)
        assertThat(retrievedGroup.users.size).isZero
        assertThat(retrievedGroup.createdAt).isBefore(Instant.now())
        assertThat(retrievedGroup.updatedAt).isAfter(retrievedGroup.createdAt)
    }

    @Test
    fun `delete group should reflect on user`() {
        val group = faker.createFakeGroup()
        val savedGroup = groupRepo.saveAndFlush(group)
        val user = faker.createFakeUser(groups = setOf(group))
        val savedUser = userRepo.saveAndFlush(user)

        entityManager.flush()
        entityManager.clear()

        assertThat(groupRepo.count()).isOne
        assertThat(userRepo.count()).isOne

        groupRepo.deleteById(group.id)

        entityManager.flush()
        entityManager.clear()

        assertThat(groupRepo.count()).isZero
        assertThat(userRepo.count()).isOne

        val retrievedUser = userRepo.findById(user.id).orElseThrow()

        assertThat(retrievedUser.id).isEqualTo(user.id)
        assertThat(retrievedUser.username).isEqualTo(user.username)
        assertThat(retrievedUser.gender).isEqualTo(user.gender)
        assertThat(retrievedUser.birthday).isEqualTo(user.birthday)
        assertThat(retrievedUser.email).isEqualTo(user.email)
        assertThat(retrievedUser.phoneNumber).isEqualTo(user.phoneNumber)
        assertThat(retrievedUser.group).isNull()
        assertThat(retrievedUser.createdAt).isBefore(Instant.now())
        assertThat(retrievedUser.updatedAt).isAfter(retrievedUser.createdAt)
    }

    @Test
    fun `updatedAt is inc on update`() {
        val user = faker.createFakeUser()
        userRepo.saveAndFlush(user)

        entityManager.flush()
        entityManager.clear()

        assertThat(userRepo.count()).isOne

        val saved = userRepo.findById(user.id).orElseThrow()
        val createdAt = saved.createdAt
        val updatedAt = saved.updatedAt
        saved.username.firstName = "_fName"
        userRepo.saveAndFlush(saved)

        entityManager.flush()
        entityManager.clear()

        assertThat(userRepo.count()).isOne

        val updated = userRepo.findById(user.id).orElseThrow()

        assertThat(updated.createdAt).isEqualTo(createdAt)
        assertThat(updated.updatedAt).isAfter(updatedAt)
    }

    @Test
    fun `composite fields are stored as columns`() {
        val user = faker.createFakeUser()
        userRepo.saveAndFlush(user)

        entityManager.flush()
        entityManager.clear()

        assertThat(jdbcTemplate.queryForObject<String>("SELECT \"firstName\" FROM \"users\""))
            .isEqualTo(user.username.firstName)
        assertThat(jdbcTemplate.queryForObject<String>("SELECT \"lastName\" FROM \"users\""))
            .isEqualTo(user.username.lastName)
        assertThat(jdbcTemplate.queryForObject<Int>("SELECT \"gender\" FROM \"users\""))
            .isEqualTo(user.gender.ordinal)
        assertThat(jdbcTemplate.queryForObject<String>("SELECT \"birthday\" FROM \"users\""))
            .isEqualTo(user.birthday.toString())
        assertThat(jdbcTemplate.queryForObject<String>("SELECT \"phoneNumber\" FROM \"users\""))
            .isEqualTo("${user.phoneNumber.prefix}#${user.phoneNumber.num}")
    }

    @Test
    fun `paging and sorting`() {
        userRepo.saveAllAndFlush(List(10) { faker.createFakeUser() })
        val sort = Sort.by(Sort.Direction.DESC, "createdAt", "username.lastName", "username.firstName")

        assertThat(userRepo.findAll(PageRequest.of(0, 5, sort)))
            .hasSize(5)
            .extracting("createdAt", Instant::class.java)
            .isSortedAccordingTo(Comparator.reverseOrder())

        assertThat(userRepo.findAll(PageRequest.of(1, 9, sort)))
            .hasSize(1)
    }
}
