package com.ecommercedemo.common.controller.concretion.permission

import com.ecommercedemo.common.controller.abstraction.RestControllerTemplate
import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.common.model.concretion.permission.Permission
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/permissions")
@ControllerFor(Permission::class)
class PermissionController : RestControllerTemplate<Permission>()