package com.hamza.thymx.groups

import com.hamza.thymx.data.BaseEntity
import com.hamza.thymx.data.EntityId
import com.hamza.thymx.data.TSIDGenerator
import com.hamza.thymx.data.decodeToTSID
import com.hamza.thymx.data.encodeToString
import com.hamza.thymx.users.User
import io.hypersistence.tsid.TSID
import jakarta.persistence.Cacheable
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.PreRemove
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.validator.constraints.Length

@Embeddable
data class GroupId(
    override val id: TSID,
) : EntityId {
    constructor() : this(TSIDGenerator.next())
    constructor(id: String) : this(id.decodeToTSID())

    override fun toString(): String = this.id.encodeToString()
}

@Entity
@Table(name = "groups")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "groups")
class Group(
    override val id: GroupId = GroupId(),
    //
    @field:Column(nullable = false, unique = true, length = 100)
    @field:NotBlank
    @field:Length(max = 100)
    var name: String,
    //
    @field:OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = [CascadeType.MERGE])
    // @field:BatchSize(16) to override globals in YAML
    @field:Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "groups.users")
    val users: MutableSet<User> = hashSetOf(),
) : BaseEntity<GroupId>(id) {
    override fun equals(other: Any?): Boolean = this === other || (other is Group && this.id == other.id)

    override fun hashCode(): Int = this.id.hashCode()

    fun addUser(user: User) {
        this.users.add(user)
        user.group = this
    }

    @PreRemove
    fun preRemove() {
        this.users.forEach {
            it.group = null
        }
    }

    fun toDto(): GroupDto =
        GroupDto(
            id = this.id.id.encodeToString(),
            name = this.name,
            users = this.users.map { it.toSubDto() },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    fun toSubDto(): GroupSubDto =
        GroupSubDto(
            id = this.id.id.encodeToString(),
            name = this.name,
        )
}
