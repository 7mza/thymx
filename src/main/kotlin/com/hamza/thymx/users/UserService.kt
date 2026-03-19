package com.hamza.thymx.users

import com.hamza.thymx.data.encodeToString
import com.hamza.thymx.groups.IGroupService
import com.hamza.thymx.shared.PageDto
import com.hamza.thymx.shared.ResourceNotFoundException
import com.hamza.thymx.shared.toDto
import jakarta.transaction.Transactional
import org.springframework.data.domain.Pageable
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

interface IUserService {
    fun findAll(pageable: Pageable): PageDto<UserDto>

    fun save(dto: CreateUserDto): UserDto

    fun findEntityById(id: String): User

    fun findSubById(id: String): UserSubDto

    fun update(
        id: String,
        dto: UpdateUserDto,
    ): UserDto

    fun deleteById(id: String)

    fun existsByEmail(email: String): Boolean

    fun existsByEmailAndIdNot(
        email: String,
        id: String,
    ): Boolean
}

@Service
@Transactional
class UserService(
    private val userRepo: IUserRepo,
    private val groupService: IGroupService,
    private val passwordEncoder: PasswordEncoder,
) : IUserService {
    override fun findAll(pageable: Pageable): PageDto<UserDto> =
        userRepo
            .findAllProjectedBy(pageable)
            .map { it.toDto() }
            .toDto()

    override fun save(dto: CreateUserDto): UserDto {
        val group =
            dto.input.groupId
                ?.takeIf { it.isNotBlank() }
                ?.let(groupService::findEntityById)
        return userRepo
            .save(dto.toEntity(group = group, passwordEncoder = passwordEncoder))
            .toDto()
    }

    override fun findEntityById(id: String): User =
        userRepo
            .findById(UserId(id))
            .orElseThrow { ResourceNotFoundException(id = id, name = "user") }

    override fun findSubById(id: String): UserSubDto =
        userRepo
            .findSubProjectedById(UserId(id))
            .orElseThrow { ResourceNotFoundException(id = id, name = "user") }
            .toSubDto()

    override fun update(
        id: String,
        dto: UpdateUserDto,
    ): UserDto {
        val user = this.findEntityById(id)
        val group =
            dto.input.groupId
                ?.takeIf { it.isNotBlank() }
                ?.let(groupService::findEntityById)
        if (dto.version != user.version) {
            throw ObjectOptimisticLockingFailureException(User::class.java, user.id.id.encodeToString())
        }
        dto.updateEntity(user = user, group = group)
        return user.toDto()
    }

    override fun deleteById(id: String) = userRepo.deleteById(UserId(id))

    override fun existsByEmail(email: String): Boolean = userRepo.existsByEmail(email)

    override fun existsByEmailAndIdNot(
        email: String,
        id: String,
    ): Boolean = userRepo.existsByEmailAndIdNot(email = email, id = UserId(id))
}
