package com.hamza.thymx.shared

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.util.unit.DataSize
import org.springframework.web.multipart.MultipartFile
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [FileSizeValidator::class])
annotation class FileSize(
    val maxSize: String = "1MB",
    val message: String = "{FileSize}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = [],
)

class FileSizeValidator : ConstraintValidator<FileSize, MultipartFile> {
    private lateinit var maxDataSize: DataSize
    private lateinit var message: String

    override fun initialize(constraintAnnotation: FileSize) {
        maxDataSize = DataSize.parse(constraintAnnotation.maxSize)
        message = constraintAnnotation.message
        super.initialize(constraintAnnotation)
    }

    override fun isValid(
        value: MultipartFile?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null || value.isEmpty) return true
        val actualDataSize = DataSize.ofBytes(value.size)
        val valid = actualDataSize <= maxDataSize
        if (!valid) {
            context.disableDefaultConstraintViolation()
            context
                .buildConstraintViolationWithTemplate(this.message)
                .addConstraintViolation()
        }
        return valid
    }
}
