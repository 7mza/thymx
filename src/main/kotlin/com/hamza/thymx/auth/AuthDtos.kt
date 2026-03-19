package com.hamza.thymx.auth

import com.hamza.thymx.shared.LaterValidationGroup
import com.hamza.thymx.users.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.validator.constraints.Length

data class LoginDto(
    @field:NotBlank
    @field:Email(groups = [LaterValidationGroup::class])
    val username: String = "",
    //
    @field:NotBlank
    @field:Length(min = 8, max = 100)
    val password: String = "",
)

data class AccountDto(
    @field:NotEmpty
    val authorities: Set<Role>,
    val accountExpired: Boolean,
    val accountLocked: Boolean,
    val credentialsExpired: Boolean,
    val enabled: Boolean,
)
