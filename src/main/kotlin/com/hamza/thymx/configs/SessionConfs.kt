package com.hamza.thymx.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.session.HttpSessionEventPublisher
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer

@Configuration
class SessionConfs {
    @Bean
    fun cookieSerializer(): CookieSerializer =
        DefaultCookieSerializer().apply {
            setUseSecureCookie(true)
            setSameSite("Strict")
            setUseHttpOnlyCookie(true)
            setCookiePath("/")
        }

    @Bean
    fun httpSessionEventPublisher(): HttpSessionEventPublisher = HttpSessionEventPublisher()
}
