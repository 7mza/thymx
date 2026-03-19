package com.hamza.thymx.web

import com.hamza.thymx.auth.IAuthenticationFacade
import com.hamza.thymx.configs.AssetManifestReader
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.propertyeditors.StringTrimmerEditor
import org.springframework.http.HttpStatus
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.ui.Model
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalModelAttributes(
    private val assetManifestReader: AssetManifestReader,
    private val navigationHelper: NavigationHelper,
    private val paginationHelper: PaginationHelper,
    private val authenticationFacade: IAuthenticationFacade,
) {
    @ModelAttribute("assetManifest")
    fun assetManifest(): Map<String, String> = assetManifestReader.getAll()

    @ModelAttribute("navbarPop")
    fun navbarPopulator(): NavbarPopulator =
        NavbarPopulator(
            menus =
                listOf(
                    NavbarMenu(
                        name = "nav.menu.groups",
                        href = "#",
                        activeFlag = "groups",
                    ),
                    NavbarMenu(
                        name = "nav.menu.users",
                        href = "/users/list",
                        activeFlag = "users",
                    ),
                    NavbarMenu(
                        name = "nav.menu.examples",
                        activeFlag = "",
                        subMenus =
                            listOf(
                                NavbarMenu(
                                    name = "nav.menu.examples.basics",
                                    href = "/basics",
                                    activeFlag = "basics",
                                ),
                                NavbarMenu(
                                    name = "nav.menu.examples.fragments",
                                    href = "/fragments",
                                    activeFlag = "fragments",
                                ),
                                NavbarMenu(
                                    name = "nav.menu.examples.layouts",
                                    href = "/layouts",
                                    activeFlag = "layouts",
                                ),
                                NavbarMenu(
                                    name = "nav.menu.examples.validations",
                                    href = "/validations",
                                    activeFlag = "validations",
                                ),
                                NavbarMenu(
                                    name = "nav.menu.examples.htmx",
                                    href = "/htmx",
                                    activeFlag = "htmx",
                                ),
                            ),
                    ),
                ),
        )

    @ModelAttribute("langPop")
    fun languagesPopulator(): LanguagesPopulator = LanguagesPopulator(languages = listOf("en", "ar", "fr"))

    @ModelAttribute("nvHelper")
    fun navigationHelper(): NavigationHelper = navigationHelper

    @ModelAttribute("pgHelper")
    fun paginationHelper(): PaginationHelper = paginationHelper

    @ModelAttribute("theme")
    fun theme(
        @CookieValue(value = "theme", required = false) theme: String?,
    ): String = if (theme.equals("dark", true)) "dark" else "light"

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflict(
        ex: Throwable,
        request: HttpServletRequest,
        model: Model,
    ): String {
        model
            .addAttribute("url", request.requestURL)
            .addAttribute("assetManifest", this.assetManifest())
            .addAttribute("navbarPop", this.navbarPopulator())
            .addAttribute("langPop", this.languagesPopulator())
            .addAttribute("nvHelper", this.navigationHelper())
            .addAttribute("authenticationFacade", this.authenticationFacade())
        return "error/409"
    }

    @ModelAttribute("authenticationFacade")
    fun authenticationFacade(): IAuthenticationFacade = authenticationFacade

    @ModelAttribute("pageSizesPop")
    fun pageSizesPopulator(): PaginationSizesPopulator = PaginationSizesPopulator(sizes = listOf(5, 10, 50, 100))

    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.registerCustomEditor(String::class.java, StringTrimmerEditor(false))
    }
}
