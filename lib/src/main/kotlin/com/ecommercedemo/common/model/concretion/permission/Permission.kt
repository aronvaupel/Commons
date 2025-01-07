package com.ecommercedemo.common.model.concretion.permission

import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.Entity
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Entity
@ConditionalOnProperty(name = ["permissions"], havingValue = "true", matchIfMissing = false)
open class Permission(
    open var serviceOfOrigin: String = "",
    open var label: String = "",
    open var description: String = ""
) : BaseEntity()