package com.hamza.thymx.groups

import com.hamza.thymx.users.UserSubDto
import java.time.Instant

data class GroupDto(
    val id: String,
    val name: String,
    val users: List<UserSubDto>?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class GroupSubDto(
    val id: String,
    val name: String,
)
