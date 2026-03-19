package com.hamza.thymx.groups

import com.hamza.thymx.shared.PageDto
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
interface IGroupApi {
    @GetMapping("/api/groups")
    fun findAll(
        @SortDefault.SortDefaults(
            value = [
                SortDefault(sort = ["name"], direction = Sort.Direction.ASC),
            ],
        )
        pageable: Pageable,
    ): PageDto<GroupDto>
}

@RestController
class GroupRestCtrl(
    private val service: IGroupService,
) : IGroupApi {
    override fun findAll(pageable: Pageable): PageDto<GroupDto> = service.findAll(pageable)
}
