package com.hamza.thymx.users

import com.hamza.thymx.auth.AccountDto
import com.hamza.thymx.data.encodeToString
import com.hamza.thymx.groups.Group
import com.hamza.thymx.groups.GroupSubDto
import com.hamza.thymx.shared.FileSize
import com.hamza.thymx.shared.LaterValidationGroup
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.time.LocalDate

data class UserDto(
    val id: String,
    val fullName: String,
    val gender: Gender,
    val birthday: String,
    val email: String,
    val phoneNumber: String,
    val group: GroupSubDto?,
    val avatar: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class UserSubDto(
    val id: String,
    val fullName: String,
)

data class InputUser(
    @field:NotBlank
    @field:Length(max = 100)
    val firstName: String = "",
    //
    @field:NotBlank
    @field:Length(max = 100)
    val lastName: String = "",
    //
    val gender: Gender = Gender.MALE,
    //
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val birthday: LocalDate = LocalDate.now(),
    //
    @field:NotBlank
    @field:Email(groups = [LaterValidationGroup::class])
    @field:Length(max = 100)
    val email: String = "",
    //
    @field:Length(min = 2, max = 6)
    val phonePrefix: String? = null,
    //
    @field:NotBlank
    @field:Length(min = 10, max = 10)
    @field:Pattern(regexp = "[0-9]*")
    val phoneNumber: String = "",
    //
    val groupId: String? = null,
    //
    @field:NotBlank
    @field:Length(max = 100)
    val password: String = "password",
    //
    @field:FileSize
    val avatar: MultipartFile? = null,
)

data class CreateUserDto(
    @field:Valid val input: InputUser = InputUser(),
) : IHasUniqueUserIdentity {
    fun toEntity(
        group: Group? = null,
        passwordEncoder: PasswordEncoder,
    ): User =
        User(
            username =
                Username(
                    firstName = this.input.firstName,
                    lastName = this.input.lastName,
                ),
            gender = this.input.gender,
            birthday = this.input.birthday,
            email = this.input.email,
            phoneNumber =
                PhoneNumber(
                    prefix = this.input.phonePrefix,
                    num = this.input.phoneNumber,
                ),
            group = group,
            account =
                Account(
                    password = passwordEncoder.encode("password")!!,
                    authorities = hashSetOf(Role.USER),
                ),
        )
}

data class UpdateUserDto(
    val id: String,
    @field:Valid val input: InputUser = InputUser(),
    @field:Valid val account: AccountDto,
    var version: Int,
) : IHasUniqueUserIdentity {
    companion object {
        fun fromEntity(user: User): UpdateUserDto =
            UpdateUserDto(
                id = user.id.id.encodeToString(),
                input =
                    InputUser(
                        firstName = user.username.firstName,
                        lastName = user.username.lastName,
                        gender = user.gender,
                        birthday = user.birthday,
                        email = user.email,
                        phonePrefix = user.phoneNumber.prefix,
                        phoneNumber = user.phoneNumber.num,
                        groupId =
                            user.group
                                ?.id
                                ?.id
                                ?.encodeToString(),
                    ),
                account =
                    AccountDto(
                        authorities = user.account.authorities,
                        accountExpired = user.account.accountExpired,
                        accountLocked = user.account.accountLocked,
                        credentialsExpired = user.account.credentialsExpired,
                        enabled = user.account.enabled,
                    ),
                version = user.version,
            )
    }

    fun updateEntity(
        user: User,
        group: Group? = null,
    ) {
        user.username.firstName = this.input.firstName
        user.username.lastName = this.input.lastName
        user.gender = this.input.gender
        user.birthday = this.input.birthday
        user.email = this.input.email
        user.phoneNumber.prefix = this.input.phonePrefix
        user.phoneNumber.num = this.input.phoneNumber
        user.group = group
        user.account.authorities = this.account.authorities.toHashSet()
        user.account.accountExpired = this.account.accountExpired
        user.account.accountLocked = this.account.accountLocked
        user.account.credentialsExpired = this.account.credentialsExpired
        user.account.enabled = this.account.enabled
        this.input.avatar
            ?.takeIf { it.isEmpty.not() }
            ?.let { user.avatar = it.bytes }
        user.version = this.version
    }
}
