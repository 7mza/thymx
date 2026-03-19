package com.hamza.thymx.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

interface IAuthenticationFacade {
    fun getAuthentication(): Authentication?
}

@Component
class AuthenticationFacade : IAuthenticationFacade {
    override fun getAuthentication(): Authentication? = SecurityContextHolder.getContext().authentication
}
