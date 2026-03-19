package com.hamza.thymx.users

import com.hamza.thymx.groups.IGroupService
import com.hamza.thymx.shared.PageDto
import com.hamza.thymx.shared.ValidationGroupSequence
import com.hamza.thymx.web.FlashPopulator
import com.hamza.thymx.web.Operation
import com.hamza.thymx.web.RadioPopulator
import com.hamza.thymx.web.Select
import com.hamza.thymx.web.SelectPopulator
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@RequestMapping(produces = [MediaType.TEXT_HTML_VALUE])
interface IUserWebApi {
    @GetMapping(path = ["/users/list"])
    fun usersList(
        model: Model,
        @SortDefault.SortDefaults(
            value = [
                SortDefault(sort = ["username.lastName"], direction = Sort.Direction.ASC),
                SortDefault(sort = ["username.firstName"], direction = Sort.Direction.ASC),
            ],
        )
        pageable: Pageable,
    ): String

    @PreAuthorize("hasAuthority('ROLE_admin')")
    @GetMapping(path = ["/users/create"])
    fun usersCreate(model: Model): String

    @PreAuthorize("hasAuthority('ROLE_admin')")
    @PostMapping(path = ["/users/create"])
    fun createUser(
        @Validated(ValidationGroupSequence::class)
        @ModelAttribute("user")
        dto: CreateUserDto,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String

    @PreAuthorize("hasAuthority('ROLE_admin')")
    @GetMapping(path = ["/users/update/{id}"])
    fun usersUpdate(
        @PathVariable
        id: String,
        model: Model,
    ): String

    @PreAuthorize("hasAuthority('ROLE_admin')")
    @PutMapping(path = ["/users/update/{id}"])
    fun updateUser(
        @PathVariable
        id: String,
        @Validated(ValidationGroupSequence::class)
        @ModelAttribute("user")
        dto: UpdateUserDto,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @PreAuthorize("hasAuthority('ROLE_admin')")
    @DeleteMapping(path = ["/users/delete/{id}"])
    fun deleteUser(
        @PathVariable
        id: String,
        redirectAttributes: RedirectAttributes,
    ): String
}

@Controller
class UserWebCtrl(
    private val userService: IUserService,
    private val groupService: IGroupService,
) : IUserWebApi {
    override fun usersList(
        model: Model,
        pageable: Pageable,
    ): String {
        val users: PageDto<UserDto> = userService.findAll(pageable)
        model.addAttribute("users", users)
        return "users/list"
    }

    override fun usersCreate(model: Model): String {
        val groups = SelectPopulator(groupService.findAllSub().map { Select(id = it.id, text = it.name) })
        val genders: RadioPopulator = Gender.toRadios()
        model
            .addAttribute("groups", groups)
            .addAttribute("genders", genders)
            .addAttribute("user", CreateUserDto())
        return "users/create"
    }

    override fun createUser(
        dto: CreateUserDto,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String =
        if (bindingResult.hasErrors()) {
            "/users/create"
        } else {
            val user: UserDto = userService.save(dto)
            redirectAttributes.addFlashAttribute(
                "flash",
                FlashPopulator(operation = Operation.CREATED, details = user.fullName),
            )
            "redirect:/users/list"
        }

    override fun usersUpdate(
        id: String,
        model: Model,
    ): String {
        val user: User = userService.findEntityById(id)
        val groups = SelectPopulator(groupService.findAllSub().map { Select(id = it.id, text = it.name) })
        val genders: RadioPopulator = Gender.toRadios()
        val authorities: SelectPopulator = Role.toSelects()
        model
            .addAttribute("groups", groups)
            .addAttribute("genders", genders)
            .addAttribute("authorities", authorities)
            .addAttribute("user", UpdateUserDto.fromEntity(user))
        return "users/update"
    }

    override fun updateUser(
        id: String,
        dto: UpdateUserDto,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String =
        if (bindingResult.hasErrors()) {
            val groups = SelectPopulator(groupService.findAllSub().map { Select(id = it.id, text = it.name) })
            val genders: RadioPopulator = Gender.toRadios()
            val authorities: SelectPopulator = Role.toSelects()
            model
                .addAttribute("groups", groups)
                .addAttribute("genders", genders)
                .addAttribute("authorities", authorities)
            "/users/update"
        } else {
            val user: UserDto = userService.update(id = id, dto = dto)
            redirectAttributes.addFlashAttribute(
                "flash",
                FlashPopulator(operation = Operation.UPDATED, details = user.fullName),
            )
            "redirect:/users/list"
        }

    override fun deleteUser(
        id: String,
        redirectAttributes: RedirectAttributes,
    ): String {
        val user: UserSubDto = userService.findSubById(id)
        userService.deleteById(id)
        redirectAttributes.addFlashAttribute(
            "flash",
            FlashPopulator(operation = Operation.DELETED, details = user.fullName),
        )
        return "redirect:/users/list"
    }
}

@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
interface IUserApi {
    @GetMapping("/api/users")
    fun findAll(
        @SortDefault.SortDefaults(
            value = [
                SortDefault(sort = ["username.lastName"], direction = Sort.Direction.ASC),
                SortDefault(sort = ["username.firstName"], direction = Sort.Direction.ASC),
            ],
        )
        pageable: Pageable,
    ): PageDto<UserDto>
}

@RestController
class UserRestCtrl(
    private val service: IUserService,
) : IUserApi {
    override fun findAll(pageable: Pageable): PageDto<UserDto> = service.findAll(pageable)
}
