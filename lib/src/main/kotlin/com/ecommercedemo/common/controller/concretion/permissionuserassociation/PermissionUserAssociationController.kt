package com.ecommercedemo.common.controller.concretion.permissionuserassociation

import com.ecommercedemo.common.controller.abstraction.RestControllerTemplate
import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.common.model.concretion.permissionuserassociation.PermissionUserAssociation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/permission-user-associations")
@ControllerFor(PermissionUserAssociation::class)
class PermissionUserAssociationController : RestControllerTemplate<PermissionUserAssociation>()
