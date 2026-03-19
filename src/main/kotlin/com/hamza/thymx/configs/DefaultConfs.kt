package com.hamza.thymx.configs

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import java.net.InetAddress

@Configuration
class DefaultConfs(
    @Value($$"${server.port}") private val port: Int,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun jacksonCustomizer(): JsonMapperBuilderCustomizer =
        JsonMapperBuilderCustomizer {
            it.findAndAddModules()
        }

    @EventListener(ApplicationReadyEvent::class)
    fun readyListener() {
        logger.info("app running at http://{}:{}", InetAddress.getLocalHost().hostAddress, port)
    }
}
