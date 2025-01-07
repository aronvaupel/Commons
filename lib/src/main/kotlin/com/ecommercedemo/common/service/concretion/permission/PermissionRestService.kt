package com.ecommercedemo.common.service.concretion.permission

import com.ecommercedemo.common.model.concretion.permission.Permission
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@RestServiceFor(Permission::class)
@Transactional
open class PermissionRestService : RestServiceTemplate<Permission>()