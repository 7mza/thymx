package com.hamza.thymx.users

import jakarta.persistence.QueryHint
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Transactional(readOnly = true)
interface IUserRepo : JpaRepository<User, UserId> {
    fun existsByEmail(email: String): Boolean

    @Query("select u from User u inner join fetch u.account.authorities left join fetch u.group where u.email = :email")
    fun findByEmail(email: String): Optional<User>

    fun existsByEmailAndIdNot(
        email: String,
        id: UserId,
    ): Boolean

    @QueryHints(QueryHint(name = "org.hibernate.cacheable", value = "true"))
    fun findAllProjectedBy(pageable: Pageable): Page<UserProjection>

    @QueryHints(QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select u from User u inner join fetch u.account.authorities left join fetch u.group where u.id = :id")
    override fun findById(id: UserId): Optional<User>

    @QueryHints(QueryHint(name = "org.hibernate.cacheable", value = "true"))
    fun findSubProjectedById(id: UserId): Optional<UserSubProjection>
}
