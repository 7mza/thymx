package com.hamza.thymx.shared

import org.springframework.core.io.ResourceLoader
import java.nio.charset.StandardCharsets
import java.util.Locale.getDefault

fun String.capitalize(): String =
    this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
    }

class FileReader(
    private val resourceLoader: ResourceLoader,
) {
    fun readFileAsString(path: String): String =
        resourceLoader
            .getResource(path)
            .inputStream
            .use { it.readAllBytes() }
            .let { String(it, StandardCharsets.UTF_8) }
}

class ThrowingMap<K, V>(
    private val delegate: Map<K, V>,
) : Map<K, V> by delegate {
    override fun get(key: K): V = delegate[key] ?: throw AssetNotFoundException(name = key.toString())
}
