package com.hamza.thymx.users

import com.hamza.thymx.auth.CustomUserDetails
import com.hamza.thymx.data.BaseEntity
import com.hamza.thymx.data.EntityId
import com.hamza.thymx.data.TSIDGenerator
import com.hamza.thymx.data.decodeToTSID
import com.hamza.thymx.data.encodeToString
import com.hamza.thymx.groups.Group
import com.hamza.thymx.shared.capitalize
import com.hamza.thymx.web.Radio
import com.hamza.thymx.web.RadioPopulator
import com.hamza.thymx.web.Select
import com.hamza.thymx.web.SelectPopulator
import io.hypersistence.tsid.TSID
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Basic
import jakarta.persistence.Cacheable
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.validator.constraints.Length
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalDate
import kotlin.io.encoding.Base64

@Embeddable
data class UserId(
    override val id: TSID,
) : EntityId {
    constructor() : this(TSIDGenerator.next())
    constructor(id: String) : this(id.decodeToTSID())

    override fun toString(): String = this.id.encodeToString()
}

@Entity
@Table(name = "users")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "users")
class User(
    override val id: UserId = UserId(),
    //
    @field:Valid
    @field:Embedded
    var username: Username,
    //
    @field:Column(nullable = false)
    @field:Enumerated(EnumType.ORDINAL)
    var gender: Gender,
    //
    @field:Column(nullable = false)
    var birthday: LocalDate,
    //
    @field:Column(nullable = false, unique = true, length = 100)
    @field:NotBlank
    @field:Email
    @field:Length(max = 100)
    var email: String,
    //
    @field:Column(nullable = false, length = 100)
    @field:Valid
    @field:Convert(converter = PhoneNumberAttributeConverter::class)
    var phoneNumber: PhoneNumber,
    //
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "group_id")
    var group: Group? = null,
    //
    @field:Embedded
    @field:Valid
    var account: Account,
    //
    @field:Lob
    @field:Basic(fetch = FetchType.LAZY)
    var avatar: ByteArray? = null,
) : BaseEntity<UserId>(id) {
    override fun equals(other: Any?): Boolean = this === other || (other is User && this.id == other.id)

    override fun hashCode(): Int = this.id.hashCode()

    fun toDto(): UserDto =
        UserDto(
            id = this.id.id.encodeToString(),
            fullName = this.username.toString(),
            gender = this.gender,
            birthday = this.birthday.toString(),
            email = this.email,
            phoneNumber = this.phoneNumber.toString(),
            group = this.group?.toSubDto(),
            avatar = this.avatar?.let { Base64.encode(it) },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    fun toSubDto(): UserSubDto =
        UserSubDto(
            id = this.id.id.encodeToString(),
            fullName = this.username.toString(),
        )

    fun toUserDetails(): CustomUserDetails =
        CustomUserDetails(
            username = this.email,
            password = this.account.password,
            authorities =
                this.account.authorities
                    .map { it.toRole() }
                    .toHashSet(),
            accountExpired = this.account.accountExpired,
            accountLocked = this.account.accountLocked,
            credentialsExpired = this.account.credentialsExpired,
            enabled = this.account.enabled,
        )
}

@Embeddable
data class Username(
    @field:Column(nullable = false, length = 100)
    @field:NotBlank
    @field:Length(max = 100)
    var firstName: String,
    //
    @field:Column(nullable = false, length = 100)
    @field:NotBlank
    @field:Length(max = 100)
    var lastName: String,
) {
    override fun toString(): String = "${this.firstName.capitalize()} ${this.lastName.capitalize()}"
}

enum class Gender(
    private val locale: String,
) {
    FEMALE("user.gender.female"),
    MALE("user.gender.male"),
    ;

    companion object {
        fun toRadios(): RadioPopulator =
            RadioPopulator(
                Gender.entries
                    .sortedBy {
                        it.ordinal
                    }.map { Radio(value = it.name, text = it.locale) },
            )
    }
}

data class PhoneNumber(
    @field:Length(max = 10)
    var prefix: String? = null,
    //
    @field:NotBlank
    @field:Length(max = 100)
    var num: String,
) {
    override fun toString(): String =
        this.prefix?.takeIf { it.isNotBlank() }?.let {
            "$it ${this.num}"
        } ?: this.num
}

@Converter(autoApply = true)
class PhoneNumberAttributeConverter : AttributeConverter<PhoneNumber, String> {
    override fun convertToDatabaseColumn(attribute: PhoneNumber): String =
        attribute.let {
            it.prefix?.let { p -> "$p#${it.num}" } ?: it.num
        }

    override fun convertToEntityAttribute(dbData: String): PhoneNumber =
        dbData.let {
            val tokens = it.split("#", limit = 2)
            if (tokens.size == 2) {
                PhoneNumber(prefix = tokens[0], num = tokens[1])
            } else {
                PhoneNumber(num = it)
            }
        }
}

@Embeddable
data class Account(
    @field:Column(nullable = false, length = 255)
    @field:NotBlank
    @field:Length(min = 8, max = 255)
    var password: String,
    //
    @field:Column(name = "role", nullable = false)
    @field:Enumerated
    @field:ElementCollection(targetClass = Role::class, fetch = FetchType.LAZY)
    @field:CollectionTable(
        name = "account_roles",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)],
    )
    @field:NotEmpty
    var authorities: MutableSet<Role> = hashSetOf(),
    //
    @field:Column(nullable = false)
    var accountExpired: Boolean = false,
    //
    @field:Column(nullable = false)
    var accountLocked: Boolean = false,
    //
    @field:Column(nullable = false)
    var credentialsExpired: Boolean = false,
    //
    @field:Column(nullable = false)
    var enabled: Boolean = true,
)

enum class Role(
    private val locale: String,
) {
    ADMIN("user.account.authorities.admin"),
    USER("user.account.authorities.user"),
    ;

    companion object {
        fun toSelects(): SelectPopulator =
            SelectPopulator(
                Role.entries
                    .sortedBy {
                        it.ordinal
                    }.map { Select(id = it.name, text = it.locale) },
            )
    }

    override fun toString(): String = this.name.lowercase()

    fun toRole(): SimpleGrantedAuthority = SimpleGrantedAuthority("ROLE_$this")

    fun toAuthority(): SimpleGrantedAuthority = SimpleGrantedAuthority(this.toString())
}
