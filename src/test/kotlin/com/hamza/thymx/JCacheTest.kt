package com.hamza.thymx

import com.hamza.thymx.groups.Group
import com.hamza.thymx.groups.IGroupRepo
import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.SessionFactory
import org.hibernate.StaleStateException
import org.hibernate.stat.Statistics
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.jpa.properties.hibernate.cache.use_second_level_cache=true"],
)
@Import(PgTestContainer::class)
class JCacheTest {
    @Autowired
    private lateinit var repo: IGroupRepo

    @Autowired
    private lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    private lateinit var txManager: PlatformTransactionManager

    private lateinit var statistics: Statistics

    private val group: Group = Group(name = "group")

    @BeforeEach
    fun beforeEach() {
        statistics = entityManagerFactory.unwrap(SessionFactory::class.java).statistics
    }

    @AfterEach
    fun afterEach() {
        repo.deleteAll()
        statistics.clear()
    }

    @Test
    fun `L2 cache should be set immediately on write and don't read from db after`() {
        assertThat(statistics.prepareStatementCount).isZero
        assertThat(statistics.secondLevelCachePutCount).isZero
        assertThat(statistics.secondLevelCacheHitCount).isZero

        // write + read
        val id = repo.saveAndFlush(group).id
        // 2 calls to db
        assertThat(statistics.prepareStatementCount).isEqualTo(2)
        // cache set
        assertThat(statistics.secondLevelCachePutCount).isOne
        // no cache read
        assertThat(statistics.secondLevelCacheHitCount).isZero

        // 1st read
        repo.findById(id)
        // no new call to db (+ 2 previous)
        assertThat(statistics.prepareStatementCount).isEqualTo(2)
        // no new cache set
        assertThat(statistics.secondLevelCachePutCount).isOne
        // cache read
        assertThat(statistics.secondLevelCacheHitCount).isOne
    }

    @Test
    fun `projection lookup never reads from L2 cache`() {
        assertThat(statistics.prepareStatementCount).isZero
        assertThat(statistics.secondLevelCachePutCount).isZero
        assertThat(statistics.secondLevelCacheHitCount).isZero

        // write + read
        val id = repo.saveAndFlush(group).id
        // 2 calls to db
        assertThat(statistics.prepareStatementCount).isEqualTo(2)
        // cache set
        assertThat(statistics.secondLevelCachePutCount).isOne
        // no cache read
        assertThat(statistics.secondLevelCacheHitCount).isZero

        // 1st read
        repo.findProjectedById(id)
        // 1 new call to db (+ 2 previous)
        assertThat(statistics.prepareStatementCount).isEqualTo(3)
        // no new cache set
        assertThat(statistics.secondLevelCachePutCount).isOne
        // no cache read
        assertThat(statistics.secondLevelCacheHitCount).isZero
    }

    @Test
    fun `concurrent update triggers optimistic lock`() {
        val id = repo.saveAndFlush(group).id

        val latch = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(2)

        val t1 =
            pool.submit {
                TransactionTemplate(txManager).execute {
                    val e1 = repo.findById(id).get()
                    latch.await()
                    e1.name = "g1"
                    repo.saveAndFlush(e1)
                }
            }

        val t2 =
            pool.submit {
                TransactionTemplate(txManager).execute {
                    val e2 = repo.findById(id).get()
                    e2.name = "g2"
                    repo.saveAndFlush(e2)
                    latch.countDown()
                }
            }

        assertThatThrownBy { t1.get() }.hasRootCauseInstanceOf(StaleStateException::class.java)
    }
}
