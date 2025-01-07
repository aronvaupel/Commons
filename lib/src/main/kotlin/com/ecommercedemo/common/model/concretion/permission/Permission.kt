package com.ecommercedemo.common.model.concretion.permission

import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.Entity
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Entity
@ConditionalOnProperty(name = ["permissions"], havingValue = "true", matchIfMissing = false)
open class Permission(
    var serviceOfOrigin: String = "",
    var label: String = "",
    var description: String = ""
) : BaseEntity()