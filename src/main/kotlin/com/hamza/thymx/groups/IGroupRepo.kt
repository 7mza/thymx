package com.hamza.thymx.groups

import jakarta.persistence.QueryHint
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Transactional(readOnly = true)
interface IGroupRepo : JpaRepository<Group, GroupId> {
    @Query("select id from Group")
    fun findIds(pageable: Pageable): Page<GroupId>

    @Query("select distinct g from Group g left join fetch g.users where g.id in :ids")
    fun findAllProjectedByIds(
        @Param("ids") ids: List<GroupId>,
    ): List<GroupProjection>

    @QueryHints(QueryHint(name = "org.hibernate.cacheable", value = "true"))
    fun findAllSubProjectedBy(): List<GroupSubProjection>

    @QueryHints(QueryHint(name = "org.hibernate.cacheable", value = "true"))
    fun findProjectedById(id: GroupId): Optional<GroupProjection>
}
