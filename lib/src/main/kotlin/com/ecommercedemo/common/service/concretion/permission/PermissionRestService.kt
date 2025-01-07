package com.ecommercedemo.common.service.concretion.permission

import com.ecommercedemo.common.model.concretion.permission.Permission
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@RestServiceFor(Permission::class)
@Transactional
@ConditionalOnProperty(name = ["permissions"], havingValue = "true", matchIfMissing = false)
open class PermissionRestService : RestServiceTemplate<Permission>()