package com.ecommercedemo.common.service.concretion.permission

import com.ecommercedemo.common.model.concretion.permission.Permission
import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.annotation.EventServiceFor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@EventServiceFor(Permission::class)
@ConditionalOnProperty(name = ["permissions"], havingValue = "true", matchIfMissing = false)
open class PermissionEventService : EventServiceTemplate<Permission>()