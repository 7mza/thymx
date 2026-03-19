package com.hamza.thymx.configs

import com.hamza.thymx.shared.FileReader
import com.hamza.thymx.shared.ThrowingMap
import com.hamza.thymx.web.NonceFilter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ResourceLoader
import org.springframework.web.servlet.LocaleContextResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.CookieLocaleResolver
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@Configuration
class WebConfs : WebMvcConfigurer {
    @Bean
    fun assetManifestReader(
        resourceLoader: ResourceLoader,
        objectMapper: ObjectMapper,
    ): AssetManifestReader =
        AssetManifestReader(
            fileReader = FileReader(resourceLoader),
            objectMapper = objectMapper,
        )

    @Bean
    fun svgTemplateResolver(): ITemplateResolver =
        SpringResourceTemplateResolver().apply {
            prefix = "classpath:/static/svg/"
            suffix = ".svg"
            setTemplateMode("XML")
        }

    @Bean
    fun localeResolver(): LocaleContextResolver =
        CookieLocaleResolver("lang").apply {
            setCookieSecure(true)
            setCookieSameSite("Lax")
            setCookieHttpOnly(false)
            setCookiePath("/")
            setCookieMaxAge(Duration.ofDays(365))
        }

    @Bean
    fun localeInterceptor(): LocaleChangeInterceptor =
        LocaleChangeInterceptor().apply {
            paramName = "lang"
        }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localeInterceptor())
    }

    @Bean
    @Profile("docker")
    fun nonceFilter(nonceFilterProperties: NonceFilterProperties): FilterRegistrationBean<NonceFilter> =
        FilterRegistrationBean(NonceFilter(nonceFilterProperties)).apply {
            addUrlPatterns(*nonceFilterProperties.include!!.toTypedArray())
        }
}

@Configuration
@ConfigurationProperties(prefix = "custom.filters.nonce")
class NonceFilterProperties {
    var exclude: List<String>? = emptyList()
    var include: List<String>? = emptyList()
}

class AssetManifestReader(
    private val fileReader: FileReader,
    private val objectMapper: ObjectMapper,
) {
    private val assetMap: Map<String, String> by lazy {
        ThrowingMap(
            delegate =
                fileReader
                    .readFileAsString("classpath:/static/dist/asset-manifest.json")
                    .let {
                        objectMapper
                            .readValue(it, object : TypeReference<Map<String, String>>() {})
                    },
        )
    }

    fun getAll(): Map<String, String> = assetMap
}
