package com.ecommercedemo.common.controller.concretion.permissionuserassociation

import com.ecommercedemo.common.controller.abstraction.RestControllerTemplate
import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.common.model.concretion.permissionuserassociation.PermissionUserAssociation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "PermissionUserAssociation API", description = "Is used to manage the associations between permissions and users")
@RestController
@RequestMapping("/permission-user-associations")
@ControllerFor(PermissionUserAssociation::class)
@ConditionalOnProperty(name = ["permissions"], havingValue = "true", matchIfMissing = false)
class PermissionUserAssociationController : RestControllerTemplate<PermissionUserAssociation>()
