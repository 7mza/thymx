package com.hamza.thymx.auth

import com.hamza.thymx.users.IUserRepo
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DbUserDetailsService(
    private val repo: IUserRepo,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        repo
            .findByEmail(username)
            .orElseThrow {
                UsernameNotFoundException("UsernameNotFoundException")
            }.toUserDetails()
}
