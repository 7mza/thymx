package com.hamza.thymx.users

import com.hamza.thymx.shared.LastValidationGroup
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@HasUniqueUserIdentity(groups = [LastValidationGroup::class])
interface IHasUniqueUserIdentity

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [HasUniqueUserIdentityValidator::class])
annotation class HasUniqueUserIdentity(
    val message: String = "{UserExisting}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = [],
)

class HasUniqueUserIdentityValidator(
    private val userService: UserService,
) : ConstraintValidator<HasUniqueUserIdentity, IHasUniqueUserIdentity> {
    private lateinit var message: String

    override fun initialize(constraintAnnotation: HasUniqueUserIdentity) {
        message = constraintAnnotation.message
        super.initialize(constraintAnnotation)
    }

    override fun isValid(
        value: IHasUniqueUserIdentity,
        context: ConstraintValidatorContext,
    ): Boolean {
        val exists =
            when (value) {
                is CreateUserDto -> userService.existsByEmail(value.input.email)
                is UpdateUserDto -> userService.existsByEmailAndIdNot(email = value.input.email, id = value.id)
                else -> true
            }
        if (exists) {
            // without: Error in object 'user' + Field error in object 'user' on field 'email'
            context.disableDefaultConstraintViolation()
            context
                .buildConstraintViolationWithTemplate(this.message)
                // without: will not tie to th #fields
                .addPropertyNode("input.email")
                .addConstraintViolation()
        }
        return !exists
    }
}
