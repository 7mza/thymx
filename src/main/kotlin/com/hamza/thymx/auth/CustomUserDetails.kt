package com.hamza.thymx.auth

import org.springframework.security.core.CredentialsContainer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class CustomUserDetails(
    private val username: String,
    private var password: String,
    // FIXME: using roles will propagate entity serialization ?
    private val authorities: Set<GrantedAuthority>,
    private val accountExpired: Boolean,
    private val accountLocked: Boolean,
    private val credentialsExpired: Boolean,
    private val enabled: Boolean,
) : UserDetails,
    CredentialsContainer {
    override fun getAuthorities(): Set<GrantedAuthority> = this.authorities

    override fun getPassword(): String = this.password

    override fun getUsername(): String = this.username

    override fun isAccountNonExpired(): Boolean = this.accountExpired.not()

    override fun isAccountNonLocked(): Boolean = this.accountLocked.not()

    override fun isCredentialsNonExpired(): Boolean = this.credentialsExpired.not()

    override fun isEnabled(): Boolean = this.enabled

    override fun eraseCredentials() {
        this.password = "[PROTECTED]"
    }
}
