package com.hamza.thymx.groups

import com.hamza.thymx.shared.PageDto
import com.hamza.thymx.shared.ResourceNotFoundException
import com.hamza.thymx.shared.toDto
import jakarta.transaction.Transactional
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface IGroupService {
    fun findAllSub(): List<GroupSubDto>

    fun findAll(pageable: Pageable): PageDto<GroupDto>

    fun findEntityById(id: String): Group
}

@Service
@Transactional
class GroupService(
    private val groupRepo: IGroupRepo,
) : IGroupService {
    override fun findAllSub(): List<GroupSubDto> =
        groupRepo
            .findAllSubProjectedBy()
            .map { it.toSubDto() }

    override fun findAll(pageable: Pageable): PageDto<GroupDto> {
        // find paginated and sorted ids
        val page: PageDto<GroupId> = groupRepo.findIds(pageable).toDto()
        val ids: List<GroupId> = page.content
        // retrieve wanted entities + lazy associations using previous ids in one query
        // SQL IN doesn't guarantee sort !
        val groups: List<GroupProjection> = groupRepo.findAllProjectedByIds(ids)
        // lookup table: [group] ->map-> [(id,group)]
        val byId: Map<GroupId, GroupProjection> = groups.associateBy { it.id }
        // rebuild using original sort: [id] ->map-> [group]
        val ordered: List<GroupProjection> = ids.mapNotNull { byId[it] }
        return PageDto(
            content = ordered.map { it.toDto() },
            page = page.page,
            sort = page.sort,
        )
    }

    override fun findEntityById(id: String): Group =
        groupRepo
            .findById(GroupId(id))
            .orElseThrow { ResourceNotFoundException(id = id, name = "group") }
}
