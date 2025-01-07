package com.ecommercedemo.common.model.concretion.permissionuserassociation

import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.*


@Entity
@Table(
    name = "permission_user_association",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["service_of_origin", "permission_id", "user_id"])
    ]
)
open class PermissionUserAssociation(
    @Column(name = "service_of_origin", nullable = false)
    var serviceOfOrigin: String = "",
    @Column(name = "permission_id", nullable = false)
    var permissionId: UUID? = null,
    @Column(name = "user_id", nullable = false)
    var userId: UUID? = null
) : BaseEntity()