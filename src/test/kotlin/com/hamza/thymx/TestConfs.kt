package com.hamza.thymx

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@TestConfiguration(proxyBeanMethods = false)
class TestConfs {
    @Suppress("DEPRECATION")
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val encoders: MutableMap<String, PasswordEncoder> = mutableMapOf()
        encoders["noop"] = NoOpPasswordEncoder.getInstance()
        return DelegatingPasswordEncoder("noop", encoders)
    }
}
