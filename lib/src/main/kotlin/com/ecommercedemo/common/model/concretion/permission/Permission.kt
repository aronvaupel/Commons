package com.ecommercedemo.common.model.concretion.permission

import com.ecommercedemo.common.model.abstraction.BaseEntity
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Entity
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Schema(description = "Represents a permission entity.")
@Entity
@ConditionalOnProperty(name = ["permissions"], havingValue = "true", matchIfMissing = false)
open class Permission(
    @Schema(description = "Service of origin", example = "UserService")
    open var serviceOfOrigin: String = "",
    @Schema(description = "Identifier for a permission", example = "CREATE_USER")
    open var label: String = "",
    @Schema(description = "Simple description of the permission and its purpose", example = "Allows the creation of a user")
    open var description: String = ""
) : BaseEntity()