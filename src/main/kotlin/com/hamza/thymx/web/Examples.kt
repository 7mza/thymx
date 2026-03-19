package com.hamza.thymx.web

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

val elements = (0..3).map { (1..10).map { ('a'..'z').random() }.joinToString("") }

data class Userr(
    val id: Int,
    val name: String,
    val isToto: Boolean,
)

val user = Userr(1, "John", true)

val map = mapOf(Pair("MA", "RBT"), Pair("US", "DC"), Pair("FR", "PRS"))

data class TestDto(
    @field:NotBlank
    val testField1: String = "",
    //
    @field:Email
    val testField2: String = "a",
    //
    @field:NotNull
    @field:DateTimeFormat(pattern = "yyyy/MM/dd")
    var testField3: LocalDate? = null,
    //
    @field:Min(1)
    @field:Max(0)
    val testField4: Int? = 5,
    //
    @field:Length(min = 1, max = 1)
    val testField5: String = "ab",
    //
    @field:Pattern(regexp = "[0-9]*")
    val testField6: String = "x",
)
