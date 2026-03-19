package com.hamza.thymx.auth

import com.hamza.thymx.configs.SecurityProperties
import com.hamza.thymx.shared.ValidationGroupSequence
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping(produces = [MediaType.TEXT_HTML_VALUE])
interface IAuthApi {
    @GetMapping(path = ["/login"])
    fun login(
        @AuthenticationPrincipal
        principal: UserDetails?,
        model: Model,
    ): String

    @PostMapping(path = ["/login"])
    fun loginUser(
        @Validated(ValidationGroupSequence::class)
        @ModelAttribute("login")
        dto: LoginDto,
        bindingResult: BindingResult,
    ): String

    @GetMapping(path = ["/logout"])
    fun logout(): String
}

@Controller
class AuthCtrl(
    private val securityProperties: SecurityProperties,
    private val authenticationFacade: IAuthenticationFacade,
) : IAuthApi {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun login(
        principal: UserDetails?,
        model: Model,
    ): String {
        model.addAttribute(
            "login",
            LoginDto(
                username = securityProperties.admin.username,
                password = securityProperties.admin.password,
            ),
        )
        principal?.let { logger.warn("Principal: {}", it) }
        val authentication: Authentication? = authenticationFacade.getAuthentication()
        return if (
            authentication?.isAuthenticated ?: false &&
            authentication !is AnonymousAuthenticationToken
        ) {
            "redirect:/"
        } else {
            "login"
        }
    }

    override fun loginUser(
        dto: LoginDto,
        bindingResult: BindingResult,
    ): String =
        if (bindingResult.hasErrors()) {
            "/login"
        } else {
            "forward:/loginFilter"
        }

    override fun logout(): String = "logout"
}
