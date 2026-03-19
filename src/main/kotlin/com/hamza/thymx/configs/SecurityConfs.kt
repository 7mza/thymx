package com.hamza.thymx.configs

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfs {
    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun authenticationManager(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder,
    ): AuthenticationManager =
        ProviderManager(
            DaoAuthenticationProvider(userDetailsService).apply {
                setPasswordEncoder(passwordEncoder)
            },
        )

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        securityProperties: SecurityProperties,
    ): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.GET, *securityProperties.permit!!.toTypedArray())
                    .permitAll()
                    .anyRequest()
                    // .authenticated()
                    .permitAll()
            }.formLogin {
                it
                    .loginPage("/login")
                    .loginProcessingUrl("/loginFilter")
                    .permitAll()
            }.logout {
                it
                    .permitAll()
                    .logoutSuccessUrl("/")
            }.csrf {
                it.csrfTokenRepository(
                    CookieCsrfTokenRepository().apply {
                        setCookieCustomizer { cookie ->
                            cookie
                                .secure(true)
                                .sameSite("Strict")
                                .httpOnly(true)
                                .path("/")
                        }
                    },
                )
            }.cors { it.disable() }
            .sessionManagement { it.maximumSessions(1) }
        return http.build()
    }
}

@Configuration
@ConfigurationProperties(prefix = "custom.security")
class SecurityProperties {
    lateinit var admin: User
    lateinit var user: User
    var permit: List<String>? = emptyList()
}

class User {
    lateinit var username: String
    lateinit var password: String
    lateinit var roles: List<String>
}
