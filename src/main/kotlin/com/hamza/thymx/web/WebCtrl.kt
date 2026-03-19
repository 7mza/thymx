package com.hamza.thymx.web

import com.hamza.thymx.shared.ValidationGroupSequence
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping(produces = [MediaType.TEXT_HTML_VALUE])
interface IWebApi {
    @GetMapping("/favicon.ico")
    fun noFavicon(): ResponseEntity<Void>

    @GetMapping(path = ["/"])
    fun index(): String

    @GetMapping(path = ["/basics"])
    fun basics(model: Model): String

    @GetMapping(path = ["/fragments"])
    fun fragments(): String

    @GetMapping(path = ["/layouts"])
    fun layouts(): String

    @GetMapping(path = ["/validations"])
    fun validations(model: Model): String

    @PostMapping(path = ["/validations"])
    fun validations(
        @Validated(ValidationGroupSequence::class)
        @ModelAttribute("test")
        dto: TestDto,
        bindingResult: BindingResult,
    ): String

    @GetMapping(path = ["/htmx"])
    fun htmx(): String
}

@Controller
class WebCtrl : IWebApi {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun noFavicon(): ResponseEntity<Void> = ResponseEntity.noContent().build()

    override fun index(): String = "index"

    override fun basics(model: Model): String {
        model
            .addAttribute("elements", elements)
            .addAttribute("user", user)
            .addAttribute("map", map)
            .addAttribute("searchTerm", "thymeleaf")
            .addAttribute("preproc", "nav.title")
        return "examples/1_basics"
    }

    override fun fragments(): String = "examples/2_fragments"

    override fun layouts(): String = "examples/3_layouts"

    override fun validations(model: Model): String {
        model.addAttribute("test", TestDto())
        return "examples/4_validations"
    }

    override fun validations(
        dto: TestDto,
        bindingResult: BindingResult,
    ): String {
        logger.debug("test: {}", dto)
        return "examples/4_validations"
    }

    override fun htmx(): String = "examples/5_htmx"
}
