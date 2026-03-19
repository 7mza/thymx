package com.hamza.thymx.shared

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

data class AssetNotFoundException(
    private val name: String,
) : RuntimeException("asset $name not found at static/dist/asset-manifest.json")

@ResponseStatus(HttpStatus.NOT_FOUND)
data class ResourceNotFoundException(
    private val id: String,
    private val name: String,
) : RuntimeException("$name: $id not found")
