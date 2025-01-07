package com.ecommercedemo.common.service.concretion.permission

import com.ecommercedemo.common.model.concretion.permission.Permission
import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.annotation.EventServiceFor
import org.springframework.stereotype.Service

@Service
@EventServiceFor(Permission::class)
open class PermissionEventService : EventServiceTemplate<Permission>()