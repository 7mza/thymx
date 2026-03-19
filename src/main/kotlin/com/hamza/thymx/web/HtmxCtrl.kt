package com.hamza.thymx.web

import net.datafaker.Faker
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Random

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class HtmxRestCtrl {
    private val faker: Faker = Faker(Random(0))

    @GetMapping(path = ["/htmx/rest"])
    fun htmx(): String = faker.animal().name()
}

@Controller
@RequestMapping(produces = [MediaType.TEXT_HTML_VALUE])
class HtmxWebCtrl {
    companion object {
        @JvmStatic
        var value = 0
    }

    @GetMapping(path = ["/htmx/web"])
    fun htmx(): String = "examples/htmx :: hello"

    @GetMapping(path = ["/htmx/web/progress"])
    fun progress(model: Model): String {
        if (value == 100) value = 0 else value += 10
        model.addAttribute("value", value)
        return "examples/htmx :: progress"
    }
}
