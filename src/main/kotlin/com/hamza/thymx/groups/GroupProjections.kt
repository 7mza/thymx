package com.hamza.thymx.groups

import com.hamza.thymx.users.UserSubProjection
import java.time.Instant

interface GroupProjection {
    val id: GroupId
    val name: String
    val users: List<UserSubProjection>
    val createdAt: Instant
    val updatedAt: Instant
    val version: Int

    fun toDto(): GroupDto =
        GroupDto(
            id = this.id.toString(),
            name = this.name,
            users = this.users.map { it.toSubDto() },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}

interface GroupSubProjection {
    val id: GroupId
    val name: String

    fun toSubDto(): GroupSubDto =
        GroupSubDto(
            id = this.id.toString(),
            name = this.name,
        )
}
