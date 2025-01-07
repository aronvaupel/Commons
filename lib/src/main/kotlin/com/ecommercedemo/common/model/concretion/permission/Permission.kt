package com.ecommercedemo.common.model.concretion.permission

import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.Entity

@Entity
open class Permission(
    var serviceOfOrigin: String = "",
    var label: String = "",
    var description: String = ""
) : BaseEntity()