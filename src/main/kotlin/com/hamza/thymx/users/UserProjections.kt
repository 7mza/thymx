package com.hamza.thymx.users

import com.hamza.thymx.groups.GroupSubProjection
import java.time.Instant
import java.time.LocalDate
import kotlin.io.encoding.Base64

interface UsernameProjection {
    val firstName: String
    val lastName: String

    fun toUsername(): Username =
        Username(
            firstName = this.firstName,
            lastName = this.lastName,
        )
}

interface PhoneNumberProjection {
    val prefix: String?
    val num: String

    fun toPhoneNumber(): PhoneNumber =
        PhoneNumber(
            prefix = this.prefix,
            num = this.num,
        )
}

interface UserProjection {
    val id: UserId
    val username: UsernameProjection
    val gender: Gender
    val birthday: LocalDate
    val email: String
    val phoneNumber: PhoneNumberProjection
    val group: GroupSubProjection?
    val avatar: ByteArray?
    val createdAt: Instant
    val updatedAt: Instant
    val version: Int

    fun toDto(): UserDto =
        UserDto(
            id = this.id.toString(),
            fullName = this.username.toUsername().toString(),
            gender = this.gender,
            birthday = this.birthday.toString(),
            email = this.email,
            phoneNumber = this.phoneNumber.toPhoneNumber().toString(),
            group = this.group?.toSubDto(),
            avatar = this.avatar?.let { Base64.encode(it) },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}

interface UserSubProjection {
    val id: UserId
    val username: UsernameProjection

    fun toSubDto(): UserSubDto =
        UserSubDto(
            id = this.id.toString(),
            fullName = this.username.toUsername().toString(),
        )
}

interface AccountProjection {
    val password: String
    val authorities: Set<Role>
    val accountNonExpired: Boolean
    val accountNonLocked: Boolean
    val credentialsNonExpired: Boolean
    val enabled: Boolean
}
